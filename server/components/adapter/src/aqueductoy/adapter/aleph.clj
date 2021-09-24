(ns aqueductoy.adapter.aleph
  (:require [aleph.http :as http]
            [integrant.core :as ig]))

(defmethod ig/init-key :aqueductoy.adapter/aleph [_ {:keys [handler server]}]
  (http/start-server (:hadndler handler) server))

(defmethod ig/halt-key! :aqueductoy.adapter/aleph [_ server]
  (.close server))
