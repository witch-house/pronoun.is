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

(defn disambiguate
  "given a row and its lexically-closest neighbors,
  determine the smallest abbreviation which is still
  distinct."
  [prev row next]
  (loop [n 1]
    (let [row-n (take n row)]
      (cond
        (>= n 5) row
        (= row-n (take n prev)) (recur (+ n 1))
        (= row-n (take n next)) (recur (+ n 1))
        :else row-n))))

(defn abbreviate
  "given a list of pronoun rows, return a list of
  pairs, where the first item is the abbreviation
  and the second is the original pronoun row."
  [sorted-table]
  (loop [acc nil
         prev nil
         row (first sorted-table)
         todo (rest sorted-table)]
    (let [next (first todo)
          abbrev (disambiguate prev row next)
          pair (list abbrev row)
          acc2 (conj acc pair)]
      (if (empty? todo) (reverse acc2)
          (recur acc2 row next (rest todo))))))
