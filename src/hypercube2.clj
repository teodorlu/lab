;; # Latin hypercube sampling
;;
;; > Latin hypercube sampling (LHS) is a statistical method for generating a
;; > near-random sample of parameter values from a multidimensional
;; > distribution. The sampling method is often used to construct computer
;; > experiments or for Monte Carlo integration.
;;
;; https://en.m.wikipedia.org/wiki/Latin_hypercube_sampling

(ns hypercube2
  (:require
   [nextjournal.clerk :as clerk]))

(defn randoms [N]
  (repeatedly N rand))

(defn timed [f]
  (let [start (System/currentTimeMillis)
        res (f)
        end (System/currentTimeMillis)]
    {:duration (- end start)
     :result res}))

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
          {:keys [duration result]} (timed #(hypercube-sample-single-variable N))]
      {"N" {:pretty (list "^" 10 exp) :num N} "avg(sample(N))" (/ (reduce + result) N) "duration (ms)" duration}))))
