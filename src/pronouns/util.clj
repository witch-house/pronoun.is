;; pronoun.is - a website for pronoun usage examples
;; Copyright (C) 2014 - 2018 Morgan Astra

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <https://www.gnu.org/licenses/>

(ns pronouns.util
  (:require [clojure.string :as s]
            [clojure.set :as set]))

(defn slurp-tabfile
  "Read a tabfile from a filesystem <path> as a table"
  [path]
  (let [lines (s/split (slurp path) #"\n")]
    (map #(s/split % #"\t") lines)))

(defn table-front-filter
  "filter a <table> to the rows which begin with <query-key>"
  [query-key table]
  (let [arity (count query-key)]
    (filter #(= query-key (take arity %)) table)))

(defn table-end-filter
  "filter a <table> to the rows which end with <query-key>"
  [query-key table]
  (let [table-arity (- (count (first table)) 1)
        query-arity (count query-key)]
    (filter #(= query-key (drop-last 1 (drop (- table-arity query-arity) %))) table)))

(defn table-lookup
  "find the row corresponding to <query-key> in <table>"
  [query-key table]
  (if (some #(= "..." %) query-key)
    (let [[query-front query-end-] (split-with #(not= "..." %) query-key)
          query-end (drop 1 query-end-)
          front-matches (table-front-filter query-front table)]
      (first (table-end-filter query-end front-matches)))
    (first (table-front-filter query-key table))))

(defn shortest-unambiguous-forward-path
  "Compute the shortest (in number of path elements) forward path which
  unambiguously refers to a specific <row> in a <table>. The behavior of
  this function is undefined if given a <row> that is not in the <table>.

  See also: shortest-unambiguous-path"
  [table row]
  (loop [n 1]
    (let [row-front (take n row)]
      (if (>= 1 (count (table-front-filter row-front table)))
        row-front
        (recur (inc n))))))

(defn shortest-unambiguous-ellipses-path
  "Compute the shortest (in number of path elements) ellipses path which
  unambiguously refers to a specific <row> in a <table>. The behavior of
  this function is undefined if given a <row> that is not in the <table>.

  See also: shortest-unambiguous-path"
  [table row]
  (let [row-end (first (take-last 2 row))
        filtered-table (table-end-filter [row-end] table)]
    (loop [n 1]
      (let [row-front (take n row)]
        (if (>= 1 (count (table-front-filter row-front filtered-table)))
          (concat row-front ["..." row-end])
          (recur (inc n)))))))

(defn shortest-unambiguous-path
  "Compute the shortest (in number of path elements) path which unambiguously
  refers to a specific <row> in a <table>. The behavior of this function is
  undefined if given a <row> that is not in the <table>.

  A path can either be a 'forward path', in which it specifies the row with
  elements from the front (e.g. ze/zir), or an 'ellipses path', which elides
  unnecessary elements from the middle (e.g. they/.../themselves). If the
  shortest forward and ellipses paths are the same length, prefer the forward
  path"
  [table row]
  (let [forward-path (shortest-unambiguous-forward-path table row)
        ellipses-path (shortest-unambiguous-ellipses-path table row)]
    (s/join "/"
            (if (> (count forward-path) (count ellipses-path))
              ellipses-path
              forward-path))))

(defn abbreviate
  "return the list of minimum unabiguous paths from a <table>"
  [table]
  (map (partial shortest-unambiguous-path table) table))

(defn vec-coerce
  "wrap a value <x> in a vector if it is not already in one. note that if
  <x> is already in a sequence for which vector? is false, this will add
  another layer of nesting."
  [x]
  (if (vector? x) x [x]))

(defn strip-markup [form]
  (s/join " " (filter string? (flatten form))))

;;; wordle stuff below just ignore lol
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;
;;;

(def words*
  (filter #(re-matches #"[a-z][a-z][a-z][a-z][a-z]" %)
          (s/split-lines (slurp "/usr/share/dict/words"))))

(def alphabet* (map identity "abcdefghijklmnopqrstuvwxyz"))

(defn has-l [words l]
  (filter #(s/includes? % (str l)) words))

(defn has-letters [words letters]
  (reduce has-l words letters))

(defn has-not-l [words l]
  (remove #(s/includes? % (str l)) words))

(defn has-not-letters [words not-letters]
  (reduce has-not-l words not-letters))

(defn match-one
  "pattern - a string (or regex pattern object) applying to whole words"
  [words pattern]
  (filter #(re-matches (re-pattern pattern) %) words))

(defn match-all
  [words patterns]
  (reduce match-one words patterns))


(defn letter-occurences
  [words letters]
  (let [occurences (for [c letters]
                     [c (count (has-l words c))])]
    (sort-by second occurences)))


(defn place-pattern
  [len place letter]
  (let [dots (repeat \.)
        before (take place dots)
        pattern* (concat before
                         (list letter)
                         dots)]
    (s/join (take len pattern*))))

(defn place-occurences
  [words alphabet len]
  (into {}
        (for [l alphabet
              c (range len)]
          (let [pp (place-pattern len c l)]
            [pp (match-one words pp)]))))

(defn place-matches
  [words place-occs word]
  (let [p (partial place-pattern (count word))
        patterns (map-indexed p word)]
    (map place-occs patterns)))

(defn score
  [words occs place-occs word]
  (let [l-weight 1
        p-weight 1

        l-score (into {} occs)
        l-score-total (reduce + (map #(get l-score % 0)
                                (set word)))
        p-score-total (reduce + (map count
                                (place-matches words place-occs word)))]
    (+ (* l-weight l-score-total)
       (* p-weight p-score-total))))

(defn score-words
  [words scored-letters]
  (let [len (count (first words))
        occs (letter-occurences words scored-letters)
        place-occs (place-occurences words scored-letters len)
        scores (for [w words]
                 [w (score words occs place-occs w)])]
    (->> scores
         (sort-by second)
         reverse
         (take 10))))

(defn asdf [x] (println x) x)

(defn next-move
  [prev-words {:keys [yes-letters not-letters patterns] :as amap}]
  (let [scored-letters (remove (set/union (set not-letters)
                                          (set yes-letters)) ;; we don't care about the occurrences of these
                               alphabet*)
        remaining-words (-> prev-words
                            (has-letters yes-letters)
                            (has-not-letters not-letters)
                            (match-all patterns))]
    (score-words remaining-words scored-letters)))

(defn yes-letter
  [l c]
  (if (= c \y) l nil))

(defn not-letter
  [l c]
  (if (= c \x) l nil))

(defn pattern-letter
  [l c]
  (case c
    \g (str l)
    \y (str "[^" l "]")
    \x "."))

(defn analyze-move
  "e.g. tares xyxxx"
  [word colors]
  {:yes-letters (set (filter identity (map yes-letter word colors)))
   :not-letters (set (filter identity (map not-letter word colors)))
   :patterns    [(s/join (map pattern-letter word colors))]})

(defn play-wordle
  []
  (let [len 5
        words words*
        alphabet alphabet*
        ]))
