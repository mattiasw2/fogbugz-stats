(defproject fogbugz-stats "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [http-kit "2.1.19"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/tools.logging "0.3.1"]
                 ;; (refresh) seems to work better if I switch to user first: (in-ns 'user)
                 ;; user=> (require '[clojure.tools.namespace.repl :refer [refresh]])
                 ;; user=> (refresh)
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/tools.cli "0.3.3"]
                 ]
  :main ^:skip-aot fogbugz-stats.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
