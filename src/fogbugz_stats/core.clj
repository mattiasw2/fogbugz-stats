(ns fogbugz-stats.core
  (:require [org.httpkit.client :as http] ; http://www.http-kit.org/client.html
  	    [clojure.data.xml :as xml]
	    [clojure.java.io :as io]
            [fogbugz-stats.fogbugz-api :as api]
            [fogbugz-stats.mw1 :as mw1]
            [fogbugz-stats.mwm :as mwm]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.pprint :as pp]

            )
  (:gen-class)
  )

(use 'clojure.tools.logging)


(def Config
  "Private config settings, i.e. not be distributed"
  ;; {:versionurl "http://......./api.xml"
  ;;  :url "http://...../api.asp"
  ;;  :email ""
  ;;  :password ""
  ;;  }
  (read-string (mw1/find-and-slurp "fogbugz-stats.config")))

  

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
