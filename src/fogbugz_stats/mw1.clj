(ns fogbugz-stats.mw1
  (:require
;;   [org.httpkit.client :as http] ; http://www.http-kit.org/client.html
;;   [clojure.data.xml :as xml]
   [clojure.java.io :as io]
;;   [clojure.tools.cli :refer [parse-opts]]
   )
  (:gen-class)
  )

(use 'clojure.tools.logging)

(defn find-and-slurp
  "Search and slurp for file in this dir, and all parents until found. Throw exception if not found. Max 5 levels"
  ([filename] (find-and-slurp filename 5 ""))
  ([filename level prefix]
   (if (< level 0) 
     (throw (Exception. (str "Not found: " filename)))
     (if (.exists (clojure.java.io/as-file (str prefix filename)))
       (slurp (str prefix filename))
       (recur filename (- level 1) (str "../" prefix))))))
