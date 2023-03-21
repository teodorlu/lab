(ns hypercube2)

(defn randoms [N]
  (map float (repeatedly N rand)))

(defn hypercube-sample-single-variable [N]
  (let [dz (/ 1 N)
        starting-points (->> (range N) (map (partial * dz)))
        small-randoms (map (partial * dz) (randoms N))
        randoms-ordered (map + starting-points small-randoms)]
    (shuffle randoms-ordered)))

(hypercube-sample-single-variable 20)

(sort (hypercube-sample-single-variable 20))
