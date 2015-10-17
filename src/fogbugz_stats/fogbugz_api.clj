(ns fogbugz-stats.fogbugz-api
  (:require [org.httpkit.client :as http]
            [fogbugz-stats.mwm :as mwm]
            [fogbugz-stats.mw1 :as mw1]
            )
  (:gen-class)
  )

(use 'clojure.tools.logging)

(mwm/defn2 api-xml-raw [url options]
  (let [res @(http/get url options)
        ;; no status for some kind of errors
        {:keys [status? error?]} res
        ]
    (if error?
      (let [err (str "api-xml Failed, exception is " error? " res:" res)]
        (error err)
        (throw (Exception. err)))
      (:body res))
    ))

;;; call fogbugz and return the XML parsed as nested maps
;;; throws exception if fails.
(mwm/defn2 api-xml [url options]
  (:response (mw1/xml-keep-tag-content (mw1/of-xml-string (api-xml-raw url options)))))

;;; check the version and abort if not "8"
(mwm/defn2 check-version [config]
  (let [options {}
        response (api-xml (:versionurl config) options)]
    (if (not= "8" (:version response)) (throw (Exception. "Wrong fogbugz version")))
    true))


;; http://www.http-kit.org/client.html
;; (def options {:timeout 200             ; ms
;;               :basic-auth ["user" "pass"]
;;               :query-params {:param "value" :param2 ["value1" "value2"]}
;;               :user-agent "User-Agent-string"
;;               :headers {"X-Header" "Value"}})
 
;;; http://www.example.com/api.asp?cmd=logon&email=xxx@example.com&password=BigMac =>
;;; <response><token>24dsg34lok43un23</token></response>
(mwm/defn2 login
  "Login into Fogbugz at config, and if successful at :token to config. 
   Otherwise, abort with :token is nil exception"
  [config]
  (let [options {:query-params {:cmd "logon", :email (:email config), :password (:password config)}}
        response (api-xml (:url config) options)]
    ;; if I do not get a token, this will fail, and exception is thrown (defn2)
    (into config {:token (:token response)})
    ))


;;; make simple commands without arguments
(mwm/defn2 cmd
  "Call fogbugz at config with cmd
   config should have a :token from login"
  [config cmd]
  (let [options {:query-params {:cmd cmd, :token (:token config)}}
        response (api-xml (:url config) options)]
    response
    ))

;;; http://help.fogcreek.com/8202/xml-api#Checking_the_API_Version_and_location
(mwm/defn2 listFilters [config] (cmd config "listFilters"))
(mwm/defn2 listProjects [config] (cmd config "listProjects"))
(mwm/defn2 listMailboxes [config] (cmd config "listMailboxes"))
(mwm/defn2 listAreas [config] (cmd config "listAreas"))
;;;(mwm/defn2  [config] (cmd config ""))

;;; list of Inbox
;;; http://examples.spreadsheetconverter.com/fogbugz/default.asp?pg=pgList&pre=preSaveFilterProject&ixProject=6
;;; type:"case" status:"open" project:"Inbox"
;;; this works, except we see 5 old cases.
;;; http://examples.spreadsheetconverter.com/fogbugz/api.asp?token=&cmd=search&q=type:"case"status:"open"project:"Inbox"&cols=sTitle,sStatus,sCustomerEmail,dtOpened&max=10000
;;; ignore if <case ixBug="35549" or below
;;  <case ixBug="35549" operations="edit,spam,assign,resolve,reply,forward,remind">
;;  <sTitle>
;;  <![CDATA[
;; Please Convert
;; ]]>
;;  </sTitle>
;;  <sStatus>
;;  <![CDATA[
;; Active
;; ]]>
;;  </sStatus>
;;  <sCustomerEmail>
;;  <![CDATA[
;; "Eric Gutbezahl" <ericg@glinkcomm.net>
;; ]]>
;;  </sCustomerEmail>
;;  </case>
;;  </cases>
;; </response>
(mwm/defn2 inbox
  "List all we know about the Inbox"
  [config]
  (let [response (api-xml (:url config) {:query-params {:token (:token config) :cmd "search", :q "type:\"case\"status:\"open\"project:\"Inbox\"", :cols "sTitle,sStatus,sCustomerEmail,dtOpened",:max 100 }})]
    response))
 

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; sample

;; fogbugz-stats.core=> (def cc (api/login Config))
;; #'fogbugz-stats.core/cc
;; fogbugz-stats.core=> (api/inbox cc)
;; {:cases {:case {:sTitle "(Untitled)", :sStatus "Active", :sCustomerEmail "\"Emma
;;  Williams\" <Emma.Williams@closeinvestments.com>", :dtOpened "2008-01-23T23:43:3
;; 2Z"}}}
;; fogbugz-stats.core=>
