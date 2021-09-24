(ns aqueductoy.logger.stdout
  (:require [aqueductoy.log :refer [Logger]]
            [integrant.core :as ig]))

(defrecord StdoutLogger [] Logger
  (log [this msg]
    (println msg)))

(defmethod ig/init-key :aqueductoy.logger/stdout [_ {:keys [handler server]}]
  )

(defmethod ig/halt-key! :aqueductoy.logger/stdout [_ server]
  )
