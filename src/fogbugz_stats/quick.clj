(ns fogbugz-stats.quick
  (:require [org.httpkit.client :as http] ; http://www.http-kit.org/client.html
  	    [clojure.data.xml :as xml]
	    [clojure.java.io :as io]
            [fogbugz-stats.fogbugz-api :as api]
            [mw.mw1 :as mw1]
            [mw.mwm :as mwm]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.pprint :as pp]

            )
  (:gen-class)
  )

(defn mysort [x] (sort x))
