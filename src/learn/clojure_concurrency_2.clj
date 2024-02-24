(ns learn.clojure-concurrency-2)

(require '[clojure.core.async :as a :refer [chan to-chan!! pipeline-blocking <!!]])

;; Use op on each input element, run 20 workers

(let [op (fn [arg] (* arg 42))
      result
      (let [concurrent 20
            output-chan (chan)
            input-coll (range 0 1000)]
        (pipeline-blocking concurrent
                           output-chan
                           (map op)
                           (to-chan!! input-coll))
        (<!! (a/into [] output-chan)))]
  {:count (count result)
   :sum (reduce + result)})
;; => {:count 1000, :sum 20979000}
