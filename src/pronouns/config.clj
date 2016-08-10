(ns pronouns.config
  (:require [pronouns.util :as u]))

(def ^:dynamic *pronouns-table*
  (u/slurp-tabfile "resources/pronouns.tab"))
