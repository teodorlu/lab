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

(defn leaf? [node]
  (number? (second node)))

(defn leaves [node]
  (cond (leaf? node) [node]
        :else (mapcat leaves (rest node))))

(leaves
 [:root
  [:a [:b [:c [:d 0] [:d 1] [:d 2]]]]
  [:a [:b [:c [:d 3] [:d 4] [:d 5]]]]])
;; => ([:d 0] [:d 1] [:d 2] [:d 3] [:d 4] [:d 5])
