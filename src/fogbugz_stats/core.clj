(ns fogbugz-stats.core
  (:require [org.httpkit.client :as http] ; http://www.http-kit.org/client.html
  	    [clojure.data.xml :as xml]
	    [clojure.java.io :as io]
            [fogbugz-stats.fogbugz-api :as api]
            [fogbugz-stats.mw1 :as mw1]
            [clojure.tools.cli :refer [parse-opts]]
            )
  (:gen-class)
  )

(use 'clojure.tools.logging)


(def Config
  "Private config settings, i.e. not be distributed"
  ;;{:url "" :token ""})
  (read-string (mw1/find-and-slurp "fogbugz-stats.config")))

  

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
