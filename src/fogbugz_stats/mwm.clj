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

;;; ignore this argument
(defn ignore-arg? [name]
  (or (q? name) (keyword? name) (= "&" (str name))))

;;; todo: but still cannot handle mw1/find-and-slurp
;;; what is wrong with the clojure compiler?
;;; it might be that I macroexpand too early or something different
(quote
 (defn find-and-slurp
   "Search and slurp for file in this dir, and all parents until found. Throw exception if not found. Max 50 levels"
   ([filename]
    {:pre [(not (nil? filename))]
     :post [#(not (nil? %))]
     }
    (find-and-slurp filename 50 ""))
   ([filename level prefix]
    {:pre [(not (nil? filename))(not (nil? prefix))(not (nil? level))]
    ;;; remove this :post row and it compiles, so why doesn't it work when using defn2?
     :post [#(not (nil? %))]
     }
    (if (< level 0) 
      (throw (Exception. (str "Not found: " filename)))
      (if (.exists (clojure.java.io/as-file (str prefix filename)))
        (slurp (str prefix filename))
        (recur filename (- level 1) (str "../" prefix)))))))

;;; return true of body contains recur
(defn recur? [fun]
  (some #(and (seq? %)(= 'recur (first %)))
        (tree-seq seq? rest fun)))

;;; add :post unless allowed-to-return-nil
;;; add :pre for each argument whose name not ending with ?
(defn build-pre-post [args no-post]
  (let [pre (into [] (for [arg (filter (complement ignore-arg?) args)] `(not (nil? ~arg))))
        pre2 (if (> (count pre) 0) {:pre pre} {})
        post (if no-post {} {:post [#(not (nil? %))]})
        ]
    (conj pre2 post)))


;;; add {:pre :post} map unless prepost-map already exists
(defn add-pre-post [clause no-post]
  (let [args (first clause)
        prepost (map? (second clause))
        rst (nthrest clause 1)]
    ;; make sure map is not the only part of the body, it might be that the function returns a map
    (if (and prepost (> (count rst) 1))
      clause
      (list args (build-pre-post args no-post) rst))))
            
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
        no-post (q? name)
        ;; disallow :post if recur occurs in clause
        ;; or wrap using loop, but :pre then only be run once?
        ;; http://dev.clojure.org/jira/browse/CLJ-1475
        clauses3 (for [clause clauses2] (add-pre-post clause (or no-post (recur? clause))))
        res `(~c1 ~name (~c2 ~@clauses3))]
    res))






(def ff1 '(mwm/defn2 find-and-slurp
  "Search and slurp for file in this dir, and all parents until found. Throw exception if not found. Max 50 levels"
  ([filename] (find-and-slurp filename 50 ""))
  ([filename level prefix]
   (if (< level 0) 
     (throw (Exception. (str "Not found: " filename)))
     (if (.exists (clojure.java.io/as-file (str prefix filename)))
       (slurp (str prefix filename))
       (recur filename (- level 1) (str "../" prefix))))))
  )

(def ff2 '(mwm/defn2 xml-keep-tag-content
  "Keep recursive map and only keep :tag and :content as key and value.
   The resulting structure looks like json."
  ([pxml]
   (if (:tag? pxml)
     (xml-keep-tag-content (:tag pxml)(:content pxml))
     pxml))
  ([tag content]
   {tag
    (if (seq? content)
      (let [res (for [pxml content] (xml-keep-tag-content pxml))]
        ;; use vector and remove singletons to make structure clearer
        (if (> (count res) 1) (into [] res) (first res)))
      content)}))
)

(def ff3 '(mwm/defn2 -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
)

(def ff4 '(mwm/defn2 -main?
  "I don't do a whole lot ... yet."
  [& args?]
  (println "Hello, World!"))
)
