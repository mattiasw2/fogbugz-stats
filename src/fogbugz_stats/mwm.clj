(ns fogbugz-stats.mwm
  (:require
   [clojure.pprint :as pp]
   [clojure.walk :as walk]
   )
  (:gen-class)
  )


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
          keyword-as-string-raw (str keyword1)
          keyword-as-string (.substring keyword-as-string-raw 1)]   
      (if (not= \? (last keyword-as-string))
        `(let [res# ~x] (assert (not (nil? res#)) ~(str keyword-as-string-raw " is nil")) res#) 
        ;; we have to remove the final ? in :foo? since just an annotation
        (let [name2 (.substring keyword-as-string 0 (- (count keyword-as-string) 1))
              keyword2 (keyword name2)]
          (list keyword2 (second x)))
        ))
    x))

(defmacro defn3 [& body]
  ;; expand first in order to make all bodies look the same, regardless of one or more clauses
  (let [defun (macroexpand-1 (cons 'defn body))
        [c1 name [c2 clauses]] defun
        clauses2 (for [clause clauses] (walk/postwalk wrap-get clause))
        res `(~c1 ~name (~c2 ~clauses2))]
    res))




;; (defn3 foo "comment" [x] {:pre [(some? x)], :post [(some? %)]} (:foo (:bar x)))



;;; should this be a macro?
;; (defn assert-not-nil
;;   "if v = nil, then abort"
;;   [v k]
;;   (assert (not (nil? v)) (str k " is nil"))
;;   v)

  

;; (walk/postwalk wrap-get f)
;; (defn foo "comment" [x] {:pre [(some? x)], :post [(some? %)]} (assert-some (:foo (assert-some (:bar x)))))

;; (def f2 '(defn foo "comment" [x] {:pre [(some? x)]  :post [(some? %)]} (:foo (:bar? x))))
;; (walk/postwalk mw1/wrap-get f)

;; (walk/postwalk mw1/wrap-get f2)
;; (def f3 '(defn foo "comment" [x] {:pre [(some? x)]  :post [(some? %)]} (:foo? (:bar x))))
