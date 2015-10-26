(defproject fogbugz-stats "0.2.0-SNAPSHOT"
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
                 ;; duplicate trace [org.clojure/tools.trace "0.7.8"]
                 [org.clojure/test.check "0.8.2"]
                 ]
  ;;; proxy to make fiddler work
  ;;; :jvm-opts ["-Xmx1g" "-server"] 
  ;;; :jvm-opts ["-Dhttp.proxyHost=localhost" "-Dhttp.proxyPort=8888"] Not working!!!
  :main ^:skip-aot fogbugz-stats.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
