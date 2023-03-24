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
   [teodorlu.clerk-hammertime.montecarlo :as mc]
   [tech.v3.datatype :as d]
   [tech.v3.datatype.functional :as f]))

(mc/histogram
 (->> (repeatedly 30 rand)
      (map (partial * 10))
      (map int)))

(mc/histogram (mc/sample 50))

(def N 1000)

(def X (mc/sample N))
(def Y (mc/sample N))

(clerk/row (clerk/caption "X" (mc/histogram X)) (clerk/caption "Y" (mc/histogram Y)))

;; from wikipedia:
;;
;;   corr(X, Y) = cov(X, y) / σ_X σ_Y
;;
;; and
;;
;;   cov(X, Y) = E[ (X-[X])(Y-E[Y])]

(f/- X (f/mean X))
(f/- Y (f/mean Y))

;; Covariance

(f/mean
 (f/*
  (f/- X (f/mean X))
  (f/- Y (f/mean Y))))

;; Correlation

(let [cov (f/mean
           (f/*
            (f/- X (f/mean X))
            (f/- Y (f/mean Y))))]
  (f// cov
     (f/* (f/standard-deviation X)
          (f/standard-deviation Y))))

(defn cov [A B]
  (f/mean
   (f/*
    (f/- X (f/mean A))
    (f/- Y (f/mean B)))))

(defn corr [A B]
  (f// (cov A B)
       (* (f/standard-deviation A)
          (f/standard-deviation B))))

(corr X Y)

(corr (mc/sample 100) (mc/sample 100))
(corr (mc/sample 100) (mc/sample 100))
(corr (mc/sample 100) (mc/sample 100))
(corr (mc/sample 100) (mc/sample 100))

(corr (mc/sample 101) (mc/sample 100))

^::clerk/no-cache
(defn sample! [N]
  (mc/sample N))

(corr (sample! 100) (sample! 100))
(corr (sample! 100) (sample! 100))
(corr (sample! 100) (sample! 100))

;; but ... it would be kinda nice to see

(mc/histogram (sample! 100))
(mc/histogram (sample! 100))
(mc/histogram (sample! 100))

;; perhaps I just want to keep the seed explicit all the time.
;;
;; 1. baseline sample all distributions.
;;      provide a seed, and named variable names.
;; 2. Then do it all with immutability.
