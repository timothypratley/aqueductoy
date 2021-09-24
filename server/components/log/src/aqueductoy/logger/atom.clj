(ns aqueductoy.logger.atom
  (:require [aqueductoy.log :refer [Logger]]))

(defrecord AtomLogger [logs] Logger
  (log [this msg]
    (swap! logs conj msg)))
