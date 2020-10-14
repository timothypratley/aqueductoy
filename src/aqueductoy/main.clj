(ns aqueductoy.main
  (:require [ring.adapter.jetty :as j]
            [aqueductoy.handler :as h])
  (:import (org.eclipse.jetty.server Server)))

(defonce *server (atom nil))

(defn start []
  (if @*server
    (println "Server is already running")
    (reset! *server
            (j/run-jetty #'h/handler {:port  3000
                                      :join? false
                                      ;;:async? true
                                      }))))

(defn stop []
  (if @*server
    (do (.stop ^Server @*server)
        (reset! *server nil))
    (println "Server is not running")))

(defn restart []
  (stop)
  (start))

(defn -main []
  (start))

(comment
  (start))
