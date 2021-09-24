(ns aqueductoy.client.db
  (:require [datascript.core :as d]))

(def schema
  {:block/uid {:db/unique :db.unique/identity}})

(def db
  (d/empty-db schema))

(def txs
  [[{:block/uid "123"
     :block/string "foo"}]
   [{:block/uid "456"
     :block/string "bar"}]])

(defn apply-txs [db txs]
  (reduce d/db-with db txs))

(println "HAHAHAHA" (apply-txs db txs))

;; idea: sync all ids and refs,
;; allow graph pull queries (with no attribute checks)
;; check whether the entities involved have data loaded, and load if not
;; execute full queries when the subset is in the working set
