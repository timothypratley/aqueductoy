(ns aqueductoy.db
  (:require [asami.core :as d]
            [clojure.core.async :as async]
            [integrant.core :as ig])
  (:import (java.util Date)))

(def seed
  ;; In Asami you must create the first entry without a '
  [{:db/ident     "t1"
    :aqueductoy/t 0}])

(defn transact [{:keys [conn tx-report-queue]} tx-data]
  (let [tx-report @(d/transact conn {:tx-data tx-data})]
    ;; if the service goes down before publishing, or publishing fails,
    ;; we would need to detect that when a new process starts up (reconcile db log with publish log).
    (async/>!! tx-report-queue tx-report)
    tx-report))

(defmethod ig/init-key :aqueductoy/db [_ {:keys [db-uri]}]
  (d/create-database db-uri)
  (let [conn (d/connect db-uri)
        buffer (async/buffer 10000)
        tx-report-queue (async/chan buffer)]
    (doto
      {:db-uri          db-uri
       :conn            conn
       :buffer          buffer
       :tx-report-queue tx-report-queue}
      (transact seed))))

(defmethod ig/halt-key! :aqueductoy/db [_ {:keys [db-uri tx-report-queue]}]
  ;; TODO: wouldn't do this in prod!
  (d/delete-database db-uri)
  (async/close! tx-report-queue))

(defn db [this]
  (d/db (:conn this)))

(defn now []
  (.getTime (Date.)))

;; TODO: query :t, add :t2 query, it should not fire
(defn update-t [this]
  (transact this [{:db/ident      "t1"
                   ;; Asami does not support Lookup Refs
                   ;;:db/id [:aqueductoy/name "t1"]
                   :aqueductoy/t' (now)}]))

(defn examine-data [this]
  (d/q '{:find  [?e ?a ?v]
         :where [[?e ?a ?v]]}
       (db this)))

(comment
  (def sys (ig/init {:aqueductoy/db {:db-uri "asami:mem://dbname"}}))
  (update-t sys)
  (ig/halt! sys))
