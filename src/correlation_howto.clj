;; # How can we correlate two variables we sample from with monte carlo simulation?
;;
;; Let's try to simulate our way to an answer.
;; This is our approach:
;;
;; 1. Sample uncorrelated variables
;; 2. Measure correlation
;; 3. Tweak sample method and remeasure correlation.

(ns correlation-howto
  (:require
   [nextjournal.clerk :as clerk]
   [teodorlu.clerk-hammertime.montecarlo :as mc]))

(mc/histogram
 (->> (repeatedly 30 rand)
      (map (partial * 10))
      (map int)))

(mc/histogram (mc/sample 50))

(def N 1000)

(def X (mc/sample N))
(def Y (mc/sample N))

;; now, how do we compute the correlation of X and Y?
