(ns fogbugz-stats.fogbugz-api
  (:require [org.httpkit.client :as http]
            [fogbugz-stats.mwm :as mwm]
            [fogbugz-stats.mw1 :as mw1]
            )
  (:gen-class)
  )

(use 'clojure.tools.logging)

(mwm/defn2 api-xml [config]
  (let [res @(http/get (:url config) {})
        ;; no status for some kind of errors
        {:keys [status? error?]} res
        ]
    (if error?
      (let [err (str "api-xml Failed, exception is " error? " res:" res)]
        (error err)
        (throw (Exception. err)))
      (:body res))
    ))


(mwm/defn2 test2 [config]
  (:version (:response (mw1/xml-keep-tag-content (mw1/of-xml-string (api-xml config))))))

;;; https://client.cdn77.com/support/api/version/2.0/data#Prefetch
(mwm/defn2 cdn77-prefetch [config urls]
  (if (not= urls ())
    (let [;;the one without the www2
          site (:cdn config)              
          ;;remove host part from urls, only keep local path
          no-site-urls (map #(clojure.string/replace % site "") urls) 
          options {:form-params {:cdn_id (:cdn_id config)
                                 :login (:login config)
                                 :passwd (:passwd config)
                                 ;;purge_first makes cdn77 replace it's current cache
                                 "purge_first" "1"
                                 "url[]" no-site-urls}}
          res @(http/post "https://api.cdn77.com/v2.0/data/prefetch" options)
          {:keys [status error]} res]
      (info "prefetched " no-site-urls)
      (if error (error "Failed, exception is " error res)))))



(mwm/defn2 cdn77-purgeall [config]
  (let [options {:form-params {:cdn_id (:cdn_id config)
                               :login (:login config)
                               :passwd (:passwd config)}}
        res @(http/post "https://api.cdn77.com/v2.0/data/purge-all" options)
        {:keys [status error]} res]
    (info "purgeall, PLEASE rerun in about 10 minutes to fill caches. ")
    (if error (error "Failed, exception is " error res))))

