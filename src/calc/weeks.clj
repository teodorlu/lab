(ns calc.weeks
  (:require
   [clojure.math :as math]))

(def extra-hours 94)

(defn whole-days [hours]
  (math/floor (/ hours 7.5)))

(def extra-days
  (whole-days extra-hours))

extra-days
;; => 12.0

(- extra-hours (* extra-days 7.5))
;; => 4.0



(math/floor (/ extra-hours 7.5))
(/ extra-hours 7.5)
