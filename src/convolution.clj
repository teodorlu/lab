(ns convolution
  (:require [nextjournal.clerk :as clerk]))

;; goal
;;
;; 1. Explain convolution
;; 2. Display how convolution is computed using a grid —
;;    will also let me learn some Clerk primitives
;; 3. Show image transformations using convolution
;;     1. Sharpen
;;     2. Blur
;;     3. Vertical change
;;     4. Gradients and laplace equation

(clerk/html [:hr])

;; Q: Am I able to implement convolution?

(defn convolute [u v]
  (let [N (dec (+ (count u) (count v)))
        collect (atom (vec (repeat N 0)))]
    (doseq [i (range (count u))
            j (range (count v))]
      (swap! collect update (+ i j)
             (fn [oldval]
               (+ oldval (* (get u i) (get v j))))))
    @collect))

(clerk/table (for [[u v expected] [[[1 2 3] [1] [1 2 3]]
                                   [[1 2 3] [10 20 30] [10 40 100 120 90]]]]
               {'u u 'v v 'expected expected 'actual (convolute u v)}))

;; A: yes I am! 😁

(clerk/html [:hr])

;; Q: Am I able to sum dices using convolution?

(let [dice-sides (vec (range 1 (inc 6)))]
  (convolute dice-sides dice-sides))

(let [dice-sides (vec (range 1 (inc 6)))]
  (convolute dice-sides (vec (reverse dice-sides))))

(let [ones (vec (repeat 6 1))]
  (convolute ones ones))

;; I think I made a mistake, where's the 7?
