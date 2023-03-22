(ns hypercube3
  (:require
   [nextjournal.clerk :as clerk]
   [tech.v3.datatype :as d]
   [tech.v3.datatype.functional :as f]
   [ham-fisted.api :as hamf])
  (:import [ham_fisted Transformables]))


(vec (d/make-reader :int32 10 idx))

(defn timed [f]
  (let [start (System/currentTimeMillis)
        res (f)
        end (System/currentTimeMillis)]
    {:duration (- end start)
     :result res}))

(defn hypercube-sample-single-variable3-mean [{:keys [N create-range create-rand]}]
  (let [dz (/ 1 N)
        starting-points (f/* dz (create-range N))
        small-randoms (f/* dz (create-rand N))
        randoms-ordered (f/+ starting-points small-randoms)]
    (f/mean randoms-ordered)))

(let [N 10]
  (vec (.toArray (.doubles (java.util.Random.) N))))

(import '[ham_fisted Transformables])

(defn hamf-rand-reducer [^long n]
  (reify
    clojure.lang.Counted
    (count [_this] n)
    clojure.lang.IReduceInit
    (reduce [_this rfn acc]
      (let [rfn (ham_fisted.Transformables/toDoubleReductionFn rfn)
            r (java.util.Random.)]
        (loop [idx 0 acc acc]
          (if (< idx n)
            (recur (unchecked-inc idx)
                   (.invokePrim rfn acc (.nextDouble r)))
            acc))))))

(let [N 10]
  (vec (hamf/double-array (hamf-rand-reducer N))))

(clerk/col
 (str "java version: " (System/getProperty "java.version"))
 (clerk/table
  (into []
        (for [config [{:name "teodorlu1"
                       :create-range range
                       :create-rand #(repeatedly % rand)}
                      {:name "daslu1"
                       :create-range (fn [N] (d/as-reader (range N)))
                       :create-rand (fn [N] (d/make-reader :float32 N (rand)))}
                      {:name "daslu2"
                       :create-range (fn [N] (d/clone (d/as-reader (range N))))
                       :create-rand (fn [N] (d/make-reader :float32 N (rand)))}
                      {:name "teodorlu2"
                       :create-range (fn [N] (d/make-reader :int32 N idx))
                       :create-rand (fn [N] (d/make-reader :float32 N (rand)))}
                      {:name "teodorlu3"
                       :create-range (fn [N] (d/make-reader :int32 N idx))
                       :create-rand (fn [N] (d/clone (d/make-reader :float32 N (rand))))}
                      {:name "chrisn1 (r.doubles())"
                       :description "java-random-doubles-array-randomness"
                       :create-range (fn [N] (d/make-reader :int32 N idx))
                       :create-rand (fn [N] (.toArray (.doubles (java.util.Random.) N)))
                       }
                      {:name "chrisn2 (hamf reducer)"
                       :description "ham_fisted.Transformables/toDoubleReductionFn reducer randomness"
                       :create-range (fn [N] (d/make-reader :int32 N idx))
                       :create-rand (fn [N] (hamf/double-array (hamf-rand-reducer N)))
                       }
                      ]]
          (let [N (Math/pow 10 6)
                config (assoc config :N N)
                {:keys [duration result]} (timed #(hypercube-sample-single-variable3-mean config))]
            {"config" (:name config)
             "abs(0.5 - avg(sample(N)))" (Math/abs (- 0.5 result))
             "duration (ms)" duration})))))

(comment
  (clerk/clear-cache!))
