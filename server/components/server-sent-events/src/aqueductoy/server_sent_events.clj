(ns aqueductoy.server-sent-events
  "Use case: Browser connections with single direction updates"
  (:require [cheshire.core :as json]
            [compojure.core :as c]
            [integrant.core :as ig]))

(defmethod ig/init-key :aqueductoy/server-sent-events [_ {:keys []}]
  {})

(defmethod ig/halt-key! :aqueductoy/server-sent-events [_ {:keys []}]
  )

(def stream-response
  (partial assoc {:status 200, :headers {"Content-Type" "text/event-stream"}} :body))

(def EOL "\n")

#_(extend-type clojure.core.async.impl.channels.ManyToManyChannel
  StreamableResponseBody
  (write-body-to-stream [channel response output-stream]
    (async/go (with-open [writer (io/writer output-stream)]
                (async/loop []
                            (when-let [msg (async/<! channel)]
                              (doto writer (.write msg) (.flush))
                              (recur)))))))

#_(defn stream-msg [payload]
  (str "data:" (json/write-str payload) EOL EOL))

(c/defroutes sse-routes
  ;;(c/POST "/")
  (c/GET "/async" []
    (fn [req res raise]
      #_(let [ch (async/chan)]
        (res (stream-response ch))
        (async/go (async/>! ch (stream-msg {:val 42}))
                  (async/<! (async/timeout 1000))
                  (async/>! ch (stream-msg {:val 100}))
                  (async/close! ch))))))

(defn notify [data])
