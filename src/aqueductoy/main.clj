(ns aqueductoy.main
  (:require [ring.adapter.jetty :as j]
            [aqueductoy.handler :as h])
  (:import (org.eclipse.jetty.server Server)))

(defonce *server (atom nil))

(defn start []
  (if @*server
    (println "Server is already running")
    (j/run-jetty h/handler {:port  3000
                            :join? false})))

(defn stop []
  (if @*server
    (.stop ^Server @*server)
    (println "Server is not running")))

(defn -main []
  (start))
