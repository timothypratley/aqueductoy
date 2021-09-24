(ns aqueductoy.services.dev.db-novelty
  "Ongoing updates are being made to our data.
  This infinite loop triggers changes to simulate that."
  (:require [aqueductoy.db :as db]
            [integrant.core :as ig]))

(defmethod ig/init-key :aqueductoy.services.dev/db-novelty [_ {:keys [db]}]
  (let [*running (atom true)
        thread (doto (Thread. (fn novelty-loop []
                                (while @*running
                                  (Thread/sleep 5000)
                                  (db/update-t db)))
                              "novelty-thread")
                 (.start))]
    {:*running *running
     :thread   thread}))

(defmethod ig/halt-key! :adapter/aleph [_ {:keys [*running]}]
  (reset! *running false))

(comment
  (ig/init {:aqueductoy.services.dev/db-novelty {:db (ig/ref :aqueductoy/db)}
            :aqueductoy/db                      {:db-uri "asami:mem://dbname"}}))
