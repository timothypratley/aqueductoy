(ns aqueductoy.query-subscriptions
  (:require [aqueductoy.db :as db]
            [clojure.core.async :as async]))

;; watching the db for change,
;; using queries to figure out notifications that need to be sent
;; storage
;; not all need to be durable

;; What is the query language? GraphQL? Custom?
;;;; tags, documents, blocks
;;;; all changes in order to a graph

(def *queries
  (atom {1 {:graph "bardia"}
         2 {:tagged-blocks ["fun"]}
         3 {:tags true}}))

(defn match-datoms [datoms attr value]
  (let [matches-value? (cond
                         (vector? value) (set value)
                         (string? value) #{value}
                         (true? value) (constantly true))]
    (filter (fn [[e a v]]
              (= a attr)
              (matches-value? v))
            datoms)))

(defn match-query [datoms {:keys [graph tags tagged-blocks] :as query}]
  (cond
    graph (match-datoms datoms :graph/name graph)
    tags (match-datoms datoms :tag/name tags)
    tagged-blocks () ;; insufficient to find block changes that have a tag
    :else (println "Unexpected query" query)))

(defn get-blocks [db tags]
  ;; query all the blocks with tag
  )

(defn notify [stuff]
  ;; maintain a queue of outgoing
  (println "NOTIFY" stuff)
  )

(defn notify-all [result]
  #_#_#_(webhooks/notify result)
      (websockets/notify result)
      (sse/notify result))

(defn on-change [{:keys [db-after tx-data] :as tx-report}]
  (println "ON_CHANGE" (keys tx-report))
  ;; are any queries affected?
  (doseq [query @*queries]
    (when-let [ms (seq (match-query tx-data query))]
      (cond
        (:graph query) (notify tx-data)
        (:tags query) (notify ms)
        (:tagged-blocks query) (notify (get-blocks db-after (map last ms)))))))

(defn start-listen-thread []
  (let [closer (async/chan)]
    (async/go-loop [[val ch] (async/alts!! [db/tx-report-queue closer])]
      (when (not= ch closer)
        (on-change val)
        (recur (async/alts!! [db/tx-report-queue closer]))))
    (fn []
      (async/close! closer))))

(comment
  (def stop (start-listen-thread))
  (stop))


