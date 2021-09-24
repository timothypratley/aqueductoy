(ns aqueductoy.services.dev.main
  (:require [integrant.core :as ig]))

(defonce *system
  (atom nil))

(defn start []
  (when (not @*system)
    (let [config (ig/read-string (slurp "dev-system.edn"))]
      (ig/load-namespaces config)
      (println "Started dev service.")
      (reset! *system (ig/init config)))))

(defn stop []
  (when @*system
    (println "Stopped dev service.")
    (ig/halt! @*system)
    (reset! *system nil)))

(defn restart []
  (stop)
  (start))

(defn -main [& args]
  (start))

(comment
  (restart))
