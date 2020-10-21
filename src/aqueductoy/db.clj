(ns aqueductoy.db
  (:require [aqueductoy.webhooks :as webhooks]
            [aqueductoy.websockets :as websockets]
            [aqueductoy.server-sent-events :as sse])
  (:import (java.util Date)))

(defn now []
  (.getTime (Date.)))

(defonce db
  (atom {:t (now)}))

;; we care about change

;; TODO: query :t, add :t2 query, it should not fire
(defn update-t []
  (let [result (swap! db assoc :t (now))]
    (webhooks/notify result)
    (websockets/notify result)
    (sse/notify result)))

(defn novelty-loop
  "Ongoing updates are being made to our data.
  This infinite loop triggers changes.
  Do not call directly, see novelty-thread."
  []
  (while true
    (Thread/sleep 5000)
    (update-t)))

(defonce novelty-thread
  (doto (Thread. novelty-loop "background-thread")
    (.start)))
