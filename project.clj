(defproject witch-house/pronouns "1.9.0-SNAPSHOT"
  :description "Pronoun.is is a web app for showing usage examples of personal pronouns in English."
  :url "http://pronouns.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [ring.middleware.logger "0.5.0"]
                 [ring/ring-devel "1.2.2"]
                 [environ "0.5.0"]
                 [hiccup "1.0.5"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]
            [lein-ring "0.9.7"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "pronouns-standalone.jar"
  :profiles {:production {:env {:production true}}}
  :ring {:handler pronouns.web/app})
