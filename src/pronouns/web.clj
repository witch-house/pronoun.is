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
            [ring.middleware.params :as params]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [pronouns.util :as u]
            [pronouns.pages :as pages]))

(def config {:default-server-port 5000
             :pronoun-table-path "resources/pronouns.tab"})
(def pronouns-table (u/slurp-tabfile (:pronoun-table-path config)))

(defroutes app-routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/html; charset=utf-8"}
        :body (pages/front pronouns-table)})

  (GET "/pronouns.css" {params :params}
     {:status 200
     :headers {"Content-Type" "text/css"}
     :body (slurp (io/resource "pronouns.css"))})

  (GET "/custom-pronouns.js" []
       {:status 200
        :headers {"Content-Type" "application/javascript"}
        :body (slurp (io/resource "custom-pronouns.js"))})

  (GET "/*" {uri :uri headers :headers}
       (if (= "application/json" (.toLowerCase (get headers "accept" "*/*")))
         {:status 200
          :headers {"Content-Type" "application/json"}
          :body (pages/pronouns uri pronouns-table :json)}
         {:status 200
          :headers {"Content-Type" "text/html; charset=utf-8"}
          :body (pages/pronouns uri pronouns-table :html)}))

  (POST "/custom-link" {form :form-params}
        (pages/custom-pronoun-submit form))

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
      trace/wrap-stacktrace
      params/wrap-params))

(defn -main []
  (let [port (Integer. (:port env
                              (:default-server-port config)))]
    (jetty/run-jetty app {:port port})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
