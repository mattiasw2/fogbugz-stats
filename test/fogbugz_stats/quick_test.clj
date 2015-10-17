(ns fogbugz-stats.quick-test)

(require '[clojure.test.check :as tc])
(require '[clojure.test.check.generators :as gen])
(require '[clojure.test.check.properties :as prop])

;;; https://github.com/clojure/test.check
;;; bra video https://www.youtube.com/watch?v=JMhNINPo__g
;;;
;;; http://clojure.github.io/test.check/
;;;
;;; how to test generators:
;;; (gen/sample (gen/vector gen/int))
;;; ([] [] [] [-3 -3] [0] [3] [-6 -4 -6] [-4 2 5 -4 6 5 7] [-3 -6 -1 -7 -6 -4 -4] [-7 -1 0 4 4 -2 -2])
;;;
;;; (gen/sample (gen/elements [:a :b :c]))


(defspec first-element-is-min-after-sorting ;; the name of the test
         100 ;; the number of iterations for test.check to test
         (prop/for-all [v (gen/not-empty (gen/vector gen/int))]
           (= (apply min v)
              (first (fogbugz-stats.quick/mysort v)))))
