(ns siunit
  (:refer-clojure :exclude [+ *])
  (:require [teodorlu.siunit.alpha1 :as siunit :refer [+ *]]
            [teodorlu.siunit.alpha1.metric-prefix :refer [k m]]))

(def N {:kg 1 :m 1 :s -2})

(* 12 N 3 :m)
