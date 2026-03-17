(ns pronouns.util-test
  (:require [pronouns.util :as util]
            [clojure.test :refer [deftest testing is are]]))

(def test-table [["ze" "hir" "hir" "hirs" "hirself" "#FF0000"]
                 ["ze" "zir" "zir" "zirs" "zirself" "#FF0000"]
                 ["she" "her" "her" "hers" "herself" "#FF0000"]
                 ["he" "him" "his" "his" "himself" "#FF0000"]
                 ["they" "them" "their" "theirs" "themselves" "#FF0000"]
                 ["they" "them" "their" "theirs" "themself" "#FF0000"]])

(deftest table-filters
  (testing "table-front-filter"
    (are [arg return] (= (util/table-front-filter arg test-table) return)
      ["she"] [["she" "her" "her" "hers" "herself" "#FF0000"]]
      ["ze"] [["ze" "hir" "hir" "hirs" "hirself" "#FF0000"]
              ["ze" "zir" "zir" "zirs" "zirself" "#FF0000"]]
      ["ze" "zir"] [["ze" "zir" "zir" "zirs" "zirself" "#FF0000"]]))

  (testing "table-end-filter"
    (are [arg return] (= (util/table-end-filter arg test-table) return)
      ["themself"] [["they" "them" "their" "theirs" "themself" "#FF0000"]]
      ["themselves"] [["they" "them" "their" "theirs" "themselves" "#FF0000"]])))

(deftest table-lookup
  (are [arg return] (= (util/table-lookup arg test-table) return)
    ["she"] ["she" "her" "her" "hers" "herself" "#FF0000"]
    ["ze"] ["ze" "hir" "hir" "hirs" "hirself" "#FF0000"]
    ["ze" "zir"] ["ze" "zir" "zir" "zirs" "zirself" "#FF0000"]
    ["they"] ["they" "them" "their" "theirs" "themselves" "#FF0000"]
    ["they" "..." "themself"] ["they" "them" "their" "theirs" "themself" "#FF0000"]))

