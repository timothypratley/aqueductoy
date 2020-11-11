(defproject aqueductoy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-cljsbuild "1.1.8"]]
  :cljsbuild {:builds {:client {:source-paths ["src"]
                                :compiler {:main          "aqueductoy.client.main"
                                           :output-dir    "resources/public/js/compiled"
                                           :output-to     "resources/public/js/compiled/main.js"
                                           :asset-path    "js/compiled"
                                           :optimizations :none}}}}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojurescript "1.10.773"]
                 [org.clojure/core.async "1.3.610"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-json "0.5.0"]
                 [compojure "1.6.2"]
                 [ring/ring-defaults "0.3.2"]
                 [com.taoensso/sente "1.16.0"]
                 [stylefruits/gniazdo "1.1.4"] ;; TODO: test dependency?
                 [cheshire "5.10.0"]
                 [aleph "0.4.6"]
                 [clj-http "3.10.3"]
                 [org.clojars.quoll/asami "1.2.6"]
                 [org.clojars.quoll/asami-loom "0.2.0"]]
  :repl-options {:init-ns aqueductoy.main})
