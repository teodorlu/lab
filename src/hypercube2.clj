(ns hypercube2
  (:require
   [nextjournal.clerk :as clerk]))

;; https://en.m.wikipedia.org/wiki/Latin_hypercube_sampling

(defn randoms [N]
  (repeatedly N rand))

(defn hypercube-sample-single-variable [N]
  (let [dz (/ 1 N)
        starting-points (->> (range N) (map (partial * dz)))
        small-randoms (map (partial * dz) (randoms N))
        randoms-ordered (map + starting-points small-randoms)]
    (shuffle randoms-ordered)))

(hypercube-sample-single-variable 20)

(sort (hypercube-sample-single-variable 20))

(clerk/table
 (into []
  (for [exp [2 3 4 5 6]]
    (let [N (Math/pow 10 exp)
          sample (hypercube-sample-single-variable N)]
      {"N" {:pretty (list "^" 10 exp) :num N} "avg(sample(N))" (/ (reduce + sample) N)}))))
