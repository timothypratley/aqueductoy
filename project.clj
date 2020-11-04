(defproject aqueductoy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.3.610"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-json "0.5.0"]
                 [compojure "1.6.2"]
                 [ring/ring-defaults "0.3.2"]
                 [cheshire "5.10.0"]
                 [aleph "0.4.6"]
                 [clj-http "3.10.3"]
                 [org.clojars.quoll/asami "1.2.5"]
                 [org.clojars.quoll/asami-loom "0.2.0"]]
  :repl-options {:init-ns aqueductoy.main})
