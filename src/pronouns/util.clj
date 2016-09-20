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

(defn table-lookup
  [query-key table]
  (let [arity (count query-key)
        filtered-table (filter #(= query-key (take arity %)) table)]
    (first filtered-table)))

(defn tabfile-lookup
  [query-key tabfile]
  (table-lookup query-key (slurp-tabfile tabfile)))

(defn minimum-unambiguous-path
  ([table sections] (minimum-unambiguous-path table sections 1))
  ([table sections number-of-sections]
    (let [sections-subset (take number-of-sections sections)
          results (filter #(= (take number-of-sections %) sections-subset)
                          table)]
      (case (count results)
        0 nil
        1 (clojure.string/join "/" sections-subset)
        (recur table sections (+ number-of-sections 1))))))

(defn abbreviate
  "given a list of pronoun rows, return a list of minimum unabiguous paths"
  [table]
  (map (partial minimum-unambiguous-path table) table))

(defn vec-coerce [x]
  (if (vector? x) x [x]))
