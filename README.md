# sudoku

Solver for 9x9 Sudokus, written in Clojure.

It derives facts from a board as well as using back-tracking when no facts
are available.

## Concepts

A *board* is a sequence of 81 *cell* maps with keys :row, :col, :val and :candidates.
The row and the column of a cell are represented by values 1 to 9.
The value is also represented by a value 1 to 9.
Each cell can either have a :val key, in which case it is treated as a *fact*.
If a cell has instead a :candidate key then it points to a set of candidate values.
A *region* is a sequence of 9 cells, either one row, one column or one 3x3 block.

The algorithm derives facts from the board. It uses two rules:

 * If a cell has only one value in its candidates set then this constitutes a new fact.
 * If a single cell has a value in its candidates set that no other
   cell in the same region has, then this value constitutes a new fact. 

The board is updated sequentially fact by fact. An update of a board with a fact has two effects:

 * One cell map with candidates is replaced by the fact map.
 * The candidates of other cells (same row, same column or same 3x3 block) are
   *narrowed*, in other words the value of the fact is disjoined from the :candidates
   set.

If the algorithm is not able to find a fact it will guess the next fact. For this it
derives guesses (= hypothetical facts) from all candidate values of all cells.
The back-tracking part of the algorithm updates the board with the first guess and 
tries to solve it. If that fails the next guess is chosen until the board is solved.

All of this happens in recursive fashion.

## Usage

Assume you have in a REPL a board defined like so

```clojure
(use 'sudoku.core)
; nil
(def board
  ["   1 5 68"
   "      7 1"
   "9 1    3 "
   "  7 26   "
   "5       3"
   "   87 4  "
   " 3    8 5"
   "1 5      "
   "79 4 1   "])
; #'user/board
```

Then you solve it and print the result by issuing

```clojure
(-> board parse-facts make-board solve pp-board)
; 473|195|268
; 856|342|791
; 921|687|534
; -----------
; 347|526|189
; 582|914|673
; 619|873|452
; -----------
; 234|769|815
; 165|238|947
; 798|451|326 
; ===========
; nil
```

## References

I never played Sudoku myself. To explore rules and solutions I found help on:

* [Sudoku Solver](http://www.john.collins.name/sudoku.html)
* [Daily Sudoku](http://www.dailysudoku.com/sudoku/)

## License

Copyright 2013 F.Riemenschneider

Distributed under the Eclipse Public License, the same as Clojure.
