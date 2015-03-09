(ns pronouns.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [ring.middleware.logger :as logger]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]))

(def config {:default-server-port 5000
             :pronoun-table-path "resources/pronouns.tab"})

(defn slurp-tabfile [path]
  (let [lines (s/split (slurp path) #"\n")]
    (map #(s/split % #"\t") lines)))

(defn lookup [inputs]
  (let [pronouns-table (slurp-tabfile (:pronoun-table-path config))
        n (count inputs)
        filtered-table (filter #(= inputs (take n %)) pronouns-table)]
    (first filtered-table)))

(defn parse-pronouns-with-lookup [pronouns-string]
  (let [inputs (s/split pronouns-string #"/")
        n (count inputs)]
    (if (>= n 5)
      (take 5 inputs)
      (lookup inputs))))

(defn render-examples-page
  ([subject object possessive-determiner possessive-pronoun reflexive]
     (s/join "\n"
             [(str subject " went to the park")
              (str "I went with " object)
              (str subject " brought " possessive-determiner " frisbee")
              (str "at least I think it was " possessive-pronoun)
              (str subject " threw it to " reflexive)]))
  ([nothing]
     "We couldn't find those pronouns in our database, please let us know to add them!"))

(defroutes app-routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body "a blurb explaining how to use this site"})
  (GET "/*" {params :params}
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body (let [pronouns (parse-pronouns-with-lookup (:* params))]
                (apply render-examples-page (or pronouns [:error])))})

  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(def app
  (-> app-routes
      logger/wrap-with-logger
      wrap-error-page
      trace/wrap-stacktrace))

(defn -main []
  (let [port (Integer. (:port env
                              (:default-server-port config)))]
    (jetty/run-jetty app {:port port})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
