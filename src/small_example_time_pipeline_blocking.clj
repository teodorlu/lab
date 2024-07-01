(ns small-example-time-pipeline-blocking
  (:refer-clojure :exclude [time])
  (:require
   [clojure.core.async :as a]
   [nextjournal.clerk :as clerk]))

{:nextjournal.clerk/visibility {:result :hide}}

(defn time* [f]
  (let [before (System/currentTimeMillis)
        value (f)
        after (System/currentTimeMillis)
        duration-millis (- after before)]
    {:value value :duration-millis duration-millis}))

(defmacro time
  "An alternative to `clojure.core/time` suitable for notebooks"
  [& body]
  `(time* (fn [] ~@body)))

(defn slow [extra-latency-ms op]
  (fn [& args]
    (Thread/sleep extra-latency-ms)
    (apply op args)))

(defn pipeline-blocking-example [n workers]
  (let [cin (a/to-chan! (range n))
        cout (a/chan)
        add-10-xf (map (partial (slow 100 +) 10))]
    (time (a/pipeline-blocking workers cout add-10-xf cin)
          (a/<!! (a/into [] cout)))))

{:nextjournal.clerk/visibility {:result :show}}

;; With n = 10 and 4 workers, we should be done in three cycles.
;; Each op takes ~100 ms, so expect a bit more than 300 ms total duration.

(pipeline-blocking-example 10 4)

;; More examples:

(defn center [& body] (clerk/html (into [:div.flex.justify-center] body)))

(center
 (clerk/table
  (for [job [{:n 10 :workers 4}
             {:n 20 :workers 3}
             {:n 500 :workers 50}]]
    (let [{:keys [n workers]} job
          duration-millis (:duration-millis (pipeline-blocking-example n workers))]
      {:n n
       :workers workers
       :duration-millis duration-millis}))))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
