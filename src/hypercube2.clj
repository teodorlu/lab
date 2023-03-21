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
   [nextjournal.clerk :as clerk]
   [tech.v3.datatype :as d]
   [tech.v3.datatype.functional :as f]
   [tech.v3.datatype.argops :as argops]))

;; ## Pure Clojure
;;
;; First, let's make it in pure Clojure, the way we know how to.

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
      {"N" {:pretty (list "^" 10 exp) :num N}
       "avg(sample(N))" (/ (reduce + result) N)
       "duration (ms)" duration}))))

;; ## Array-oriented dtype-next

(defn randoms2 [N]
  (d/->array (repeatedly N rand)))

(randoms2 10)

(defn range2 [N]
  (d/->array (range N)))

(range2 10)

(defn hypercube-sample-single-variable2-mean [N]
  (let [dz (/ 1 N)
        ;; starting-points (->> (range N) (map (partial * dz)))
        starting-points (f/* dz (range2 N))
        ;; small-randoms (map (partial * dz) (randoms N))
        small-randoms (f/* dz (randoms2 N))
        ;; randoms-ordered (map + starting-points small-randoms)
        randoms-ordered (f/+ starting-points small-randoms)]
    ;;todo shuffle
    (f/mean randoms-ordered)))

(clerk/table
 (into []
  (for [exp [2 3 4 5 6]]
    (let [N (Math/pow 10 exp)
          {:keys [duration result]} (timed #(hypercube-sample-single-variable2-mean N))]
      {"N" {:pretty (list "^" 10 exp) :num N}
       "avg(sample(N))" result
       "duration (ms)" duration}))))
