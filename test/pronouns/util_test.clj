(ns pronouns.util-test
  (:require [pronouns.util :as util]
            [clojure.test :refer [deftest testing is]]))

(def test-table [
                 ["ze" "hir" "hir" "hirs" "hirself"]
                 ["ze" "zir" "zir" "zirs" "zirself"]
                 ["she" "her" "her" "hers" "herself"]
                 ["he" "him" "his" "his" "himself"]
                 ["they" "them" "their" "theirs" "themselves"]
                 ["they" "them" "their" "theirs" "themself"]])

(deftest table-filters
  (testing "table-front-filter"
    (are [arg return] (= (table-front-filter arg test-table) return)
      ["she"] [["she" "her" "her" "hers" "herself"]]
      ["ze"] [["ze" "hir" "hir" "hirs" "hirself"]
              ["ze" "zir" "zir" "zirs" "zirself"]]
      ["ze" "zir"] [["ze" "zir" "zir" "zirs" "zirself"]]))

  (testing "table-end-filter"
    (are [arg return] (= (table-end-filter arg test-table) return)
      ["themself"] [["they" "them" "their" "theirs" "themself"]]
      ["themselves" [["they" "them" "their" "theirs" "themselves"]]])))

(deftest table-lookup
  (are [arg return] (= (table-lookup arg test-table) return)
    ["she"] ["she" "her" "her" "hers" "herself"]
    ["ze"] ["ze" "hir" "hir" "hirs" "hirself"]
    ["ze" "zir"] ["ze" "zir" "zir" "zirs" "zirself"]
    ["they"] ["they" "them" "their" "theirs" "themselves"]
    ["they" "..." "themself"] ["they" "them" "their" "theirs" "themself"]))

