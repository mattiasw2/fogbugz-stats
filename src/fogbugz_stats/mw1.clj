(ns fogbugz-stats.mw1
  (:require
   ;;   [org.httpkit.client :as http] ; http://www.http-kit.org/client.html
   [clojure.xml :as c-xml]
   [clojure.data.xml :as xml]
   [clojure.java.io :as io]
;;   [clojure.tools.cli :refer [parse-opts]]
   )
  (:gen-class)
  )

(require '[clojure.pprint :as pp]
         '[clojure.walk :as walk])

;; (use 'clojure.tools.logging)

(defn find-and-slurp
  "Search and slurp for file in this dir, and all parents until found. Throw exception if not found. Max 50 levels"
  ([filename] (find-and-slurp filename 50 ""))
  ([filename level prefix]
   (if (< level 0) 
     (throw (Exception. (str "Not found: " filename)))
     (if (.exists (clojure.java.io/as-file (str prefix filename)))
       (slurp (str prefix filename))
       (recur filename (- level 1) (str "../" prefix))))))

(defn of-xml-string
  "Convert a UTF-8 string into an clojure structure"
  [str]
  (xml/parse (java.io.ByteArrayInputStream. (.getBytes str "UTF-8"))))

;; (def x1 (mw1/of-xml-string (:body @(http/get (:url Config)))))
;; (xml-keep-tag-content x1)

(defn xml-keep-tag-content
  "Keep recursive map and only keep :tag and :content as key and value.
   The resulting structure looks like json."
  ([pxml]
   {:pre [(some? pxml)] :post [(some? %)]}
   (if (:tag pxml)
     (xml-keep-tag-content (:tag pxml)(:content pxml))
     pxml))
  ([tag content]
   {:pre [(some? tag)(some? content)] :post [(some? %)]}
   {tag
    (if (seq? content)
      (let [res (for [pxml content] (xml-keep-tag-content pxml))]
        ;; use vector and remove singletons to make structure clearer
        (if (> (count res) 1) (into [] res) (first res)))
      content)}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; I hate nil:s, and I do not expect them unless I ask for them
;;; function that returns nil should have name ending with ?
;;; (:foo x) assumes :foo exits unless you write (:foo? x)
;;;
;;; I will fix this by implementing my own defn
;;; (or should is it better to do at "(fn" level?
;;; For each argument whose parameter name not ending with ?, add
;;; {:pre [(some? arg1) (some? arg2) ...]
;;; if name not ending with '?' add  THIS MEANS I CANNOT CHECK IT FOR EMBEDDED '(FN' SINCE NO NAME
;;; {:post [(some? %)]
;;; Only add if no :pre or :post

;;  (def f '(defn foo "comment" [x] {:pre [(some? x)]  :post [(some? %)]} (:foo (:bar x))))
;;  (macroexpand f)
;;   (def foo (clojure.core/fn ([x] {:pre [(some? x)], :post [(some? %)]} (:foo (:bar x)))))


;; need to handle namespace (name :foo/bar)  => "bar"
;; (namespace :foo/bar) => "foo"
(defn wrap-get [x]
  (if (and (list? x)
           (= 2 (count x))
           (keyword? (first x)))
    (let [keyword1 (first x)
          ;; (str) better than (name) + (name-space), but need to remove ':'
          keyword-as-string (.substring (str keyword1) 1)]   
      (if (not= \? (last keyword-as-string))
        ;; if I move this, I need to update mw1
        (list 'mw1/assert-not-nil x keyword-as-string)
        ;; we have to remove the final ? in :foo? since just an annotation
        (let [name2 (.substring keyword-as-string 0 (- (count keyword-as-string) 1))
              keyword2 (keyword name2)]
          (list keyword2 (second x)))
        ))
    x))

;;; should this be a macro?
(defn assert-not-nil
  "if v = nil, then abort"
  [v k]
  (assert (not (nil? v)) (str k " is nil"))
  v)

  

;; (walk/postwalk wrap-get f)
;; (defn foo "comment" [x] {:pre [(some? x)], :post [(some? %)]} (assert-some (:foo (assert-some (:bar x)))))

;; (def f2 '(defn foo "comment" [x] {:pre [(some? x)]  :post [(some? %)]} (:foo (:bar? x))))
;; (walk/postwalk mw1/wrap-get f)

;; (walk/postwalk mw1/wrap-get f2)
;; (def f3 '(defn foo "comment" [x] {:pre [(some? x)]  :post [(some? %)]} (:foo? (:bar x))))
