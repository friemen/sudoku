(ns sudoku.core-test
  (:use clojure.test
        sudoku.core))

(def board1
  ["3     5  "
   "  1 9   8"
   "  9  83 6"
   " 1 5 96  "
   "96     25"
   "  38 6 1 "
   "2 57  1  "
   "1   4 9  "
   "  6     2"])

(def solution1
  "382467591\n641395278\n759218346\n814529637\n967134825\n523876419\n295783164\n178642953\n436951782")

(deftest solve-test
  (is (= solution1
         (-> board1 parse-facts make-board solve str-board))))
