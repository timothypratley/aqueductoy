(ns aqueductoy.db
  (:require [aqueductoy.webhooks :as webhooks]
            [aqueductoy.websockets :as websockets]
            [aqueductoy.server-sent-events :as sse]
            [asami.core :as d]
            [clojure.core.async :as async])
  (:import (java.util Date)))

(defonce *conn
  (atom nil))

(def seed
  ;; In Asami you must create the first entry without a '
  [{:db/ident "t1"
    :aqueductoy/t 0}
   {:movie/title        "Explorers"
    :movie/genre        "adventure/comedy/family"
    :movie/release-year 1985}
   {:movie/title        "Demolition Man"
    :movie/genre        "action/sci-fi/thriller"
    :movie/release-year 1993}
   {:movie/title        "Johnny Mnemonic"
    :movie/genre        "cyber-punk/action"
    :movie/release-year 1995}
   {:movie/title        "Toy Story"
    :movie/genre        "animation/adventure"
    :movie/release-year 1995}])

(defonce buffer
  (async/buffer 10000))

(defonce tx-report-queue
  (async/chan buffer))

(defn publish [tx-report]
  (async/>!! tx-report-queue tx-report))

(defn transact [tx-data]
  (let [tx-report @(d/transact @*conn {:tx-data tx-data})]
    ;; if the service goes down before publishing, or publishing fails,
    ;; we would need to detect that when a new process starts up (reconcile db log with publish log).
    (publish tx-report)
    tx-report))

(defn init [{:keys [db-uri]}]
  (d/create-database db-uri)
  (reset! *conn (d/connect db-uri))
  (transact seed)
  :ok)
;;(init {:db-uri "asami:mem://dbname"})

(defn db []
  (d/db @*conn))

(defn now []
  (.getTime (Date.)))

;; we care about change

;; TODO: query :t, add :t2 query, it should not fire
(defn update-t []
  (transact [{:db/ident      "t1"
              ;; Asami does not support Lookup Refs
              ;;:db/id [:aqueductoy/name "t1"]
              :aqueductoy/t' (now)}]))

(defn examine-data []
  (d/q '{:find  [?e ?a ?v]
         :where [[?e ?a ?v]]}
       (db)))

(defonce *running
  (atom false))

(defn novelty-loop
  "Ongoing updates are being made to our data.
  This infinite loop triggers changes.
  Do not call directly, see novelty-thread."
  []
  (while @*running
    (Thread/sleep 5000)
    (update-t)))

(defn start-novelty-thread []
  (when (not @*running)
    (reset! *running true)
    (doto (Thread. novelty-loop "novelty-thread")
      (.start))
    (fn []
      (reset! *running false))))

(comment
  (start-novelty-thread))
