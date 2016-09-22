;; pronoun.is - a website for pronoun usage examples
;; Copyright (C) 2014 - 2016 Morgan Astra

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>

(ns pronouns.util
  (:require [clojure.string :as s]))

(defn slurp-tabfile [path]
  "read a tabfile from a filesystem <path> as a table"
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
  (let [table-arity (count (first table))
        query-arity (count query-key)]
    (filter #(= query-key (drop (- table-arity query-arity) %)) table)))

(defn table-lookup
  "find the row corresponding to <query-key> in <table>"
  [query-key table]
  (if (some #(= "..." %) query-key)
    (let [[query-front query-end-] (split-with #(not= "..." %) query-key)
          query-end (drop 1 query-end-)
          front-matches (table-front-filter query-front table)]
      (first (table-end-filter query-end front-matches)))
    (first (table-front-filter query-key table))))

(defn minimum-unambiguous-path
  "compute the shortest (in number of path elements) path which refers to
  a specific <row> in a <table> unambiguously."
  ([table row] (minimum-unambiguous-path table row 1))
  ([table row number-of-row]
    (let [row-subset (take number-of-row row)
          results (filter #(= (take number-of-row %) row-subset)
                          table)]
      (case (count results)
        0 nil
        1 (clojure.string/join "/" row-subset)
        (recur table row (+ number-of-row 1))))))

(defn abbreviate
  "return the list of minimum unabiguous paths from a <table>"
  [table]
  (map (partial minimum-unambiguous-path table) table))

(defn vec-coerce [x]
  "wrap a value <x> in a vector if it is not already in one. note that if
  <x> is already in a sequence for which vector? is false, this will add
  another layer of nesting."
  (if (vector? x) x [x]))
