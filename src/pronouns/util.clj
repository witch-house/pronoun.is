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
  (let [lines (s/split (slurp path) #"\n")]
    (map #(s/split % #"\t") lines)))

(defn table-front-filter
  [query-key table]
  (let [arity (count query-key)]
    (filter #(= query-key (take arity %)) table)))

(defn table-end-filter
  [query-key table]
  (let [table-arity (count (first table))
        query-arity (count query-key)]
    (filter #(= query-key (drop (- table-arity query-arity) %)) table)))

(defn table-lookup
  [query-key table]
  (if (some #(= "..." %) query-key)
    (let [[query-front query-end-] (split-with #(not= "..." %) query-key)
          query-end (drop 1 query-end-)
          front-matches (table-front-filter query-front table)]
      (first (table-end-filter query-end front-matches)))
    (first (table-front-filter query-key table))))

(defn tabfile-lookup
  [query-key tabfile]
  (table-lookup query-key (slurp-tabfile tabfile)))

(defn minimum-unambiguous-path
  ([table columns] (minimum-unambiguous-path table columns 1))
  ([table columns number-of-columns]
    (let [columns-subset (take number-of-columns columns)
          results (filter #(= (take number-of-columns %) columns-subset)
                          table)]
      (case (count results)
        0 nil
        1 (clojure.string/join "/" columns-subset)
        (recur table columns (+ number-of-columns 1))))))

(defn abbreviate
  "given a list of pronoun rows, return a list of minimum unabiguous paths"
  [table]
  (map (partial minimum-unambiguous-path table) table))

(defn vec-coerce [x]
  (if (vector? x) x [x]))
