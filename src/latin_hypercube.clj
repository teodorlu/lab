;; # Latin hypercube sampling

(ns latin-hypercube
  (:require
   [nextjournal.clerk :as clerk]
   [tech.v3.datatype :as d]
   [tech.v3.datatype.functional :as f]))

;; ## A few convenience helpers

(defn randoms [n]
  (repeatedly n rand))

(defn baseline->dice
  "map uniformly from range [0-1) to (1, 2, 3, 4, 5, 6)"
  [d] (f/ceil (f/* 6 d)))

(defn histogram-1
  "histogram with automatic buckets, simply insert a list of values"
  [values]
  (clerk/vl {:data {:values (for [v values] {:x v})}
             :mark :bar
             :encoding {:x {:bin true
                            :field :x}
                        :y {:aggregate :count}}
             :embed/opts {:actions false}}))

(defn roll-dice [n]
  (-> (randoms n)
      (d/->float-array)
      (baseline->dice)))

;; ## Let's throw our dice!
;;
;; When we throw a low number of dice, the results look uneven.

(histogram-1 (roll-dice 4))
(histogram-1 (roll-dice 40))

;; Whereas with a large number of dice, the results "even out".

(histogram-1 (roll-dice 400))
(histogram-1 (roll-dice 4000))
