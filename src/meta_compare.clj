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

(defn meta->explicit-2 [x]
  (prewalk (fn [v]
             (if (and (instance? clojure.lang.IMeta v) (some? (meta v)))
               {::value (with-meta v nil)
                ::meta (meta v)}
               {::value v}))
           x))

(defn wrapped-value? [v]
  (or (contains? v ::value)
      (contains? v ::meta)))

(comment
  ;; the following is experimental and does not yet work.
  (defn prune-non-meta [x]
    ;; goal.
    ;;
    ;; - we want to prune values without any metadata.
    ;;
    ;; logic.
    ;;
    ;; 1. We traverse outside -> in, leaf nodes first.
    ;;
    ;; 2. When we encounter a leaf node (its value is not itself a wrapped value)
    ;;    and the value has no metadata
    ;;    , we replace it with ::prune.
    ;;
    ;; 1. leaf values without metadata are replaced with ::prune.
    ;;
    (clojure.walk/postwalk (fn [_]
                             (cond () ()))
                           ()))

  :rcf)
