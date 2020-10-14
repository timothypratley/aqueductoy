(ns aqueductoy.webhooks
  "Use case: Third party implementation of a service"
  (:refer-clojure :exclude [send]))

;; queries are [:page 100]

;; This is a map of user identifiers to maps of queries and callbacks
;; '{user-id {query callback-url}}
(defonce *subscriptions
  (atom {}))

(defn add-subscription
  [user-id query callback-url]
  (swap! *subscriptions update user-id assoc query callback-url))

(defn send [data]
  (doseq [sub @*subscriptions]
    ;; TODO: call the callback-url
    (println "hello" sub)))


;; TODO: better usage patterns for handling streams/channels
;; very abstract
