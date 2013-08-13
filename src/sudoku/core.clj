(ns sudoku.core)

;; http://www.dailysudoku.com/sudoku/examples.shtml
(def sample1
  ["   1 5 68"
   "      7 1"
   "9 1    3 "
   "  7 26   "
   "5       3"
   "   87 4  "
   " 3    8 5"
   "1 5      "
   "79 4 1   "])

;; http://www.dailysudoku.co.uk/sudoku/archive/2013/08/2013-08-10.shtml
(def sample2
  [" 16  95 4"
   "  4    26"
   "8    4   "
   "  79   4 "
   "   781   "
   " 8   53  "
   "   5    1"
   "19    4  "
   "4 51  86 "])

;; http://www.dailysudoku.com/sudoku/archive/2013/08/2013-08-11.shtml
(def sample3
  ["3     5  "
   "  1 9   8"
   "  9  83 6"
   " 1 5 96  "
   "96     25"
   "  38 6 1 "
   "2 57  1  "
   "1   4 9  "
   "  6     2"])

;; input / output

(defn parse-facts
  [svec]
  (for [r (range 9) :let [vals (vec (map #(- (int %) 48) (nth svec r)))],
        c (range 9) :when (pos? (nth vals c))]
    {:row (inc r) :col (inc c) :val (nth vals c)}))


(defn str-cell
  [cell]
  (if-let [v (:val cell)]
    (str v)
    " "))


(defn str-row
  [sep cells]
  (->> cells
       (sort-by :col)
       (map str-cell)
       (partition 3)
       (interpose sep)
       (map (partial apply str))
       (apply str)))


(defn str-board
  ([board]
     (str-board "" "" board))
  ([vsep hsep board]
     (->> board
          (sort-by :row)
          (partition 9)
          (map (partial str-row vsep))
          (partition 3)
          (interpose (list (apply str (repeat 11 hsep))))
          (apply concat)
          (remove empty?)
          (clojure.string/join "\n"))))


(defn pp-board
  [board]
  (println (str-board "|" "-" board)
           "\n==========="))


;; factories

(declare update-board)

(defn make-cell
  [r c candidates]
  {:row r :col c :candidates candidates})


(defn make-fact
  [cell v]
  {:row (:row cell) :col (:col cell) :val v})


(defn make-board
  ([facts]
     (reduce (fn [board fact]
               (update-board board fact))
             (make-board)
             facts))
  ([]
     (let [candidates (set (range 1 10))]
       (for [r (range 1 10) c (range 1 10)]
         (make-cell r c candidates)))))


;; queries

(defn block
  [cell]
  (inc (+ (* (quot (dec (:row cell)) 3) 3)
          (quot (dec (:col cell)) 3))))

(defn region
  [selector value board]
  (filter #(= value (selector %)) board))


(defn regions
  [board]
  (for [sel [:row :col block], v (range 1 10)]
    (->> board (region sel v))))


;; predicates

(defn same-row? [c1 c2] (= (:row c1) (:row c2)))
(defn same-col? [c1 c2] (= (:col c1) (:col c2)))
(defn same-block? [c1 c2] (= (block c1) (block c2)))
(defn replaces? [cell fact] (and (same-row? cell fact) (same-col? cell fact)))
(defn affected? [cell fact] (or (same-row? cell fact) (same-col? cell fact) (same-block? cell fact)))
(defn solved? [board] (every? :val board))
(defn error? [board] (->> board
                          regions
                          (map :val)
                          (remove nil?)
                          (not-every? #(apply distinct? %))))

;; solver

(defn narrow
  [cell fact]
  (let [nc (update-in cell [:candidates] #(disj % (:val fact)))]
    (if (:candidates cell)
      nc
      (dissoc nc :candidates))))


(defn update-cell
  [cell fact]
  (cond
   (replaces? cell fact) fact
   (affected? cell fact) (narrow cell fact)
   :else cell))


(defn update-board
  [board fact]
  (map #(update-cell % fact) board))


(defn derive-guesses
  [board]
  (->> board
       (mapcat (fn [cell]
                 (map (partial make-fact cell)
                      (:candidates cell))))))


(defn derive-facts
  [board]
  (concat (->> board  ; get cells with singleton values
               (filter #(= 1 (count (:candidates %))))
               (map #(make-fact % (-> % :candidates first))))
          (->> board  ; get cells with exclusive value in region
               regions
               (mapcat (fn [region]
                         (->> region
                              derive-guesses
                              (group-by :val)
                              (map second)
                              (filter #(= 1 (count %)))
                              (map first)))))))


(declare solve)

(defn guess
  [board]
  (loop [guesses (derive-guesses board)]
    (if (empty? guesses)
      nil  ; no guesses left -> no solution
      (if-let [b (-> board
                     (update-board (first guesses))
                     solve)]
        b  ; found solution!, otherwise: continue with next guess
        (recur (rest guesses))))))


(defn solve
  [board]
  (loop [b board]
    (if (or (nil? b) (solved? b))
      b
      (let [facts (derive-facts b)]
        (if (empty? facts)
          (guess b)  ; no facts -> guessing remains, otherwise use fact and continue
          (recur (update-board b (first facts))))))))


;; for use in a REPL
#_(-> sample2 parse-facts make-board solve pp-board)
