(ns pronouns.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [ring.middleware.logger :as logger]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.params :as params]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [pronouns.util :as u]
            [pronouns.pages :as pages]))

(defroutes app-routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (pages/front)})

  (GET "/pronouns.css" []
     {:status 200
     :headers {"Content-Type" "text/css"}
     :body (slurp (io/resource "pronouns.css"))})

  (GET "/*" {params :params}
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (pages/pronouns params)})

  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           (binding [*out* *err*]
             (println e)
             {:status 500
              :headers {"Content-Type" "text/html"}
              :body (slurp (io/resource "500.html"))})))))

(def app
  (-> app-routes
      logger/wrap-with-logger
      wrap-error-page
      trace/wrap-stacktrace
      params/wrap-params))

(defn -main []
  (let [port (Integer. (:port env))]
    (jetty/run-jetty app {:port port})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
