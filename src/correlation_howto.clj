;; # How can we correlate to variables we sample from with monte carlo simulation?
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
