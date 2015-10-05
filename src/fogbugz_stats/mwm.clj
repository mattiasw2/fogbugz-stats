(ns fogbugz-stats.mwm
  (:require
   ;; [clojure.pprint :as pp]
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

;; (mwm/defn2 bar "no nils" ([x] (:foo (:bar x))) ([y z] (:gegga y)))
;; (mwm/defn2 bar ":gegga might be nil" ([x] (:foo (:bar x))) ([y z] (:gegga? y)))
;; (mwm/defn2 foo? "can return nil" ([x] (:foo (:bar x))) ([y z] (:gegga y)))

;;; wrap all (:xxx yyy) calls and make sure result is non-nil
(defn wrap-get [x]
  (if (and (list? x)
           (= 2 (count x))
           (keyword? (first x)))
    (let [keyword1 (first x)
          ;; (str) better than (name) + (name-space), but need to remove ':'
          keyword-as-string-raw (str keyword1)
          ;; todo: do we have a bug here and do not hande ::foo?????
          keyword-as-string (.substring keyword-as-string-raw 1)]   
      (if (not= \? (last keyword-as-string))
        `(let [res# ~x] (assert (not (nil? res#)) ~(str keyword-as-string-raw " is nil")) res#) 
        ;; we have to remove the final ? in :foo? since just an annotation
        (let [name2 (.substring keyword-as-string 0 (- (count keyword-as-string) 1))
              keyword2 (keyword name2)]
          (list keyword2 (second x)))
        ))
    x))

;;; return true if names ends with ?
(defn q? [name]
  ;; (str) in case we get a keyword
  (= \? (last (str name))))


;;; add :post unless allowed-to-return-nil
;;; add :pre for each argument whose name not ending with ?
(defn build-pre-post [args allowed-to-return-nil]
  (conj
   (if allowed-to-return-nil {} {:post #(not (nil? %))})
   {:pre (into [] (for [arg (filter (complement q?) args)] `(not (nil? ~arg))))}))


;;; add {:pre :post} map unless prepost-map already exists
(defn add-pre-post [clause allowed-to-return-nil]
  (let [args (first clause)
        prepost (map? (second clause))
        rst (nthrest clause 1)]
    (if prepost
      clause
      (list args (build-pre-post args allowed-to-return-nil) rst))))
            
;;; just like defn, except that it hates nils in (:xxx ??) lookups and
;;; in function arguments and function return values
;;; argument names ending with ? and function names ending with ? can be nil
;;;
;;; comment disappears when macroexpanding, but (doc XXX) works
(defmacro defn2 [& body]
  ;; expand first in order to make all bodies look the same, regardless of one or more clauses
  (let [defun (macroexpand-1 (cons 'defn body))
        [c1 name [c2 & clauses]] defun
        clauses2 (for [clause clauses] (walk/postwalk wrap-get clause))
        allowed-to-return-nil (q? name)
        clauses3 (for [clause clauses2] (add-pre-post clause allowed-to-return-nil))
        res `(~c1 ~name (~c2 ~clauses3))]
    res))






