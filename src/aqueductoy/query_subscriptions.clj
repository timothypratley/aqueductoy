(ns aqueductoy.query-subscriptions
  (:require [aqueductoy.db :as db]))

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
    tagged-blocks ()  ;; insufficient to find block changes that have a tag
    :else (println "Unexpected query" query)))

(defn get-blocks [db tags]
  ;; query all the blocks with tag
  )

(defn notify [stuff]
  ;; maintain a queue of outgoing
  )

(defn on-change [{:keys [db-after tx-datoms]}]
  ;; are any queries affected?
  (doseq [query @*queries]
    (when-let [ms (seq (match-query tx-datoms query))]
      (cond
        (:graph query) (notify tx-datoms)
        (:tags query) (notify ms)
        (:tagged-blocks (notify (get-blocks db-after (map last ms))))))))
