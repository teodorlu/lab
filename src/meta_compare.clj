(ns meta-compare
  (:require
   [clojure.walk :refer [prewalk]]))

(defn meta->explicit [x]
  (prewalk (fn [v]
             (if (and (instance? clojure.lang.IMeta v) (some? (meta v)))
               {::value (with-meta v nil)
                ::meta (meta v)}
               v))
           x))

(meta->explicit ^:superduper {})
;; => #:meta-compare{:value {}, :meta {:superduper true}}

(meta->explicit
 ^:very-nice-map
 {:x 1
  :y ^:the-very-best {:name "infinite"}})
;; => #:meta-compare{:value
;;                   {:x 1,
;;                    :y
;;                    #:meta-compare{:value {:name "infinite"},
;;                                   :meta {:the-very-best true}}},
;;                   :meta {:very-nice-map true}}

;; what happens with recursive metadata?

(meta->explicit
 ^{:rating 99
   :comment ^{:author "teodorlu"} {:text "very nice"}}
 {:my "object"})
;; => #:meta-compare{:value {:my "object"},
;;                   :meta
;;                   {:rating 99,
;;                    :comment
;;                    #:meta-compare{:value {:text "very nice"},
;;                                   :meta {:author "teodorlu"}}}}

(defn equal-value-and-equal-meta [& args]
  (apply = (map meta->explicit args)))

(equal-value-and-equal-meta ^:nice {:x 1} ^:nice {:x 1})
;; => true

(equal-value-and-equal-meta ^:nice {:x 1} ^:not-nice {:x 1})
;; => false
