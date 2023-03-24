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
   [nextjournal.clerk :as clerk]))

(defn histogram
  "histogram with automatic buckets, simply insert a list of values"
  [values]
  (clerk/vl {:data {:values (for [v values] {:x v})}
             :mark :bar
             :encoding {:x {:bin true
                            :field :x}
                        :y {:aggregate :count}}
             :embed/opts {:actions false}}))

(histogram (->> (repeatedly 30 rand)
                (map (partial * 10))
                (map int)))
