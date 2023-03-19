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

(frequencies (roll-dice 4000))
(frequencies (roll-dice 40000))

;; **near instant**:
(frequencies (roll-dice 400000))

;; **about a second**:
(frequencies (roll-dice 4000000))
(frequencies (roll-dice 5000000))

(defn display-dice-sum
  ([N] (display-dice-sum N {}))
  ([N opts]
   (let [d1 (roll-dice N)
         d2 (roll-dice N)
         dsum (f/+ d1 d2)
         dsum-freq (frequencies dsum)]
     (clerk/caption (clerk/tex (or (:label opts) (str "N = " N)))
                    (clerk/vl {:data {:values (for [[throw freq] dsum-freq]
                                                {"throw sum" throw "frequency" freq})}
                               :mark :bar
                               :encoding {:x {:field "throw sum"}
                                          :y {:field "frequency" :type :quantitative}}
                               :embed/opts {:actions false}})))))

(->
 (apply
  clerk/row
  (map (fn [exp] (display-dice-sum (f/pow 10 exp)
                                   {:label (str "N = 10^" exp)}))
       [1 2 3 4 5 6]))
 (assoc :nextjournal/width :full))
