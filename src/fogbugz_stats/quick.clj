(ns fogbugz-stats.quick
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

(defn mysort [x] (sort x))
