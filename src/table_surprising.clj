(ns table-surprising
  (:require [nextjournal.clerk :as clerk]))

(defn convolute [u v]
  (let [N (dec (+ (count u) (count v)))
        collect (atom (vec (repeat N 0)))]
    (doseq [i (range (count u))
            j (range (count v))]
      (swap! collect update (+ i j)
             (fn [oldval]
               (+ oldval (* (get u i) (get v j))))))
    @collect))

(defn index-starting-at [start-at coll]
  (vec (range start-at (+ (count coll) start-at))))

(let [ones (vec (repeat 6 1))
      convolution (convolute ones ones)]
  (clerk/table {'index (index-starting-at 2 convolution)
                'convolution convolution
                'probability (mapv (fn [x] (/ x 36)) convolution)
                'p2 (map (fn [x] (/ x 36)) convolution)}))

;; Isn't `p2` supposed to show up?
;;
;; If I use `mapv` instead of `map`, it does:

(let [ones (vec (repeat 6 1))
      convolution (convolute ones ones)]
  (clerk/table {'index (index-starting-at 2 convolution)
                'convolution convolution
                'probability (mapv (fn [x] (/ x 36)) convolution)
                'p2 (mapv (fn [x] (/ x 36)) convolution)}))

;; Minimise the error.

(let [ones (vec (repeat 6 1))
      convolution (convolute ones ones)]
  (clerk/table {'index (index-starting-at 2 convolution)
                'p2 (map (fn [x] (/ x 36)) convolution)}))

(clerk/table {'index [1 2 3]
              'p2 (list 1 2 3)})
