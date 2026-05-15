(ns pronouns.e2e-test
  (:require [clojure.test :refer [deftest testing is]]
            [clj-http.lite.client :as client]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [clojure.java.shell :refer [sh]]
            [clojure.java.process :as p]
            [clojure.java.io :as io]
            [manifold.stream :as stream]))

(defn tcp-bound? [port]
  (zero? (:exit (sh "fuser" (str port "/tcp")))))

(defn find-open-port []
  (loop [port 3000]
    (if-not (tcp-bound? port)
      port
      (recur (+ 1024 (rand-int 8900))))))

(defn process->log-stream [^java.lang.Process process]
  (let [log-stream (stream/stream)]
    (future
      (try
        (with-open [log-reader (-> process p/stderr io/reader)]
          (loop [lines (line-seq log-reader)]
            (when-let [line (first lines)]
              (stream/put! log-stream line)
              (recur (rest lines)))))
        (finally (stream/close! log-stream))))
    log-stream))

(defn read-logs-and-kill [^java.lang.Process process]
  (let [log-stream (process->log-stream process)
        log-lines (atom [])]
    (future (loop []
              (when-let [line @(stream/take! log-stream)]
                (swap! log-lines conj line)
                (recur))))

    (Thread/sleep 100) ;; Let the future reading the stream catch up
    (.destroy process)
    (stream/close! log-stream)

    (let [exit @(p/exit-ref process)] ;; Wait for the process to die
      (log/debug "Exit code: " exit))

    (string/join "\n" @log-lines)))

(defn wait-for-server [timeout-ms port server-process]
  (let [start-time (System/currentTimeMillis)
        exp-time (+ timeout-ms start-time)]

    (log/info "Waiting for server to start on" port start-time exp-time)
    (loop []
      (when (> (System/currentTimeMillis) exp-time)
        (log/warn "Server not up after" timeout-ms)
        (log/warn "*** server-process logs below ***" timeout-ms)
        (let [log-lines (read-logs-and-kill server-process)]
          (log/warn log-lines)
          (log/warn "*** server-process logs above ***")
          (throw (ex-info "Server startup timeout" {}))))
      (Thread/sleep 500)
      (if (tcp-bound? port)
        (do (log/info "Server process took"
                      (- (System/currentTimeMillis) start-time)
                      "ms to bind port")
            true)
        (recur)))))

(defn start-server [port]
  (p/start {:env {"PORT" (str port)
                  "CLASSPATH" ""}}
           "lein" "run"))

(defn get-response [port path]
  (client/get (str "http://localhost:" port path)
              {:socket-timeout 5000
               :conn-timeout 5000
               :throw-exceptions false}))

(defn assert-status-and-body [port test-cases]
  (loop [test-cases test-cases]
    (when-let [case (first test-cases)]
      (let [response (get-response port (:path case))]
        (testing (str "Response for " (:label case))
          (is (= (:status case) (:status response)))
          (is (re-find (:body-re case) (:body response)))))
      (recur (rest test-cases)))))

(defn assert-logs [final-logs test-cases]
  (loop [test-cases test-cases]
    (when-let [case (first test-cases)]
      (when (:log-re case)
        (testing (str "Log matches for " (:label case))
          (is (re-find (:log-re case) final-logs))))
      (recur (rest test-cases)))))

(def e2e-server-test-cases
  [{:label "Front page"
    :path "/"
    :status 200
    :body-re #"<a href=\"all-pronouns\">"
    :log-re #":request-method :get, :uri \"/\""}

   {:label "Error page"
    :path "/DEBUG-FORCE-500"
    :status 500
    :body-re #"Something went wrong"
    :log-re #"DEBUG-FORCE-500 error occurred!"}

   {:label "ze/hir pronouns page"
    :path "/ze/hir"
    :status 200
    :body-re #"ze/hir"}

   {:label "they/.../themselves pronouns page"
    :path "/they/.../themselves"
    :status 200
    :body-re #"they/them"}

   {:label "Teapot"
    :path "/coffee"
    :status 418
    :body-re #"cannot brew coffee"}

   {:label "Not found page"
    :path "/asdf/foo/bar"
    :status 200
    :body-re #"Not Found"}

   {:label "Unknown URL parameter"
    :path "/they?foo=bar"
    :status 200
    :body-re #"they/them"}

   {:label "Pronoun with parameter alternate"
    :path "/they?or=she"
    :status 200
    :body-re #"they.*she"}

   {:label "Pronoun with path alternate"
    :path "/they/:or/she"
    :status 200
    :body-re #"they.*she"}

   {:label "Pronoun with two alternates"
    :path "/they?or=she&or=ze"
    :status 200
    :body-re #"they.*she.*ze"}

   {:label "Non-database pronoun with 5 path segments"
    :path "/a/b/c/d/e"
    :status 200
    :body-re #"a/b examples"}

   {:label "Multiple slashes"
    :path "///"
    :status 200
    :body-re #"<a href=\"all-pronouns\">"}])

(defn cleanup
  "Ensure <port> is unbound + shutdown-agents"
  [port]
  (sh "fuser" "-k" (str port "/tcp"))
  (shutdown-agents))

(deftest ^:e2e e2e-server-test
  (let [maxwait 120000
        port (find-open-port)
        server-process (start-server port)]

    (wait-for-server maxwait port server-process)

    (testing "Boot server in child process, get page responses"
      (assert-status-and-body port e2e-server-test-cases))

    (testing "Server logs"
      (let [final-log (read-logs-and-kill server-process)]

        (log/debug "*** Subprocess log follows ***\n" final-log)
        (assert-logs final-log e2e-server-test-cases)

          ;; Nothing should ever crash the server
        (is (not (re-find #"FATAL" final-log)))))

    (cleanup port)))
