(ns pronouns.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.logger :as logger]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]))

(def config {:server-port 5000})

(defroutes app-routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/plain"}
        :body "a blurb explaining how to use this site"})
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
      ; logger/wrap-with-logger
      wrap-error-page
      trace/wrap-stacktrace))

(defn -main []
  (jetty/run-jetty app {:port (:server-port config)}))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
