(ns small-example-time-pipeline-blocking
  (:refer-clojure :exclude [time])
  (:require
   [clojure.core.async :as a]
   [nextjournal.clerk :as clerk]
   [java-time-literals.core]
   [nextjournal.clerk.viewer])
  (:import (java.time Duration)))

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

;; java.time.Duration i stedet for tall i implisitt enhet

(defn time2* [f]
  (let [before (System/currentTimeMillis)
        value (f)
        after (System/currentTimeMillis)
        duration-millis (- after before)]
    {:value value :duration (Duration/ofMillis duration-millis)}))

(defmacro time2
  "An alternative to `clojure.core/time` suitable for notebooks"
  [& body]
  `(time2* (fn [] ~@body)))

(defn pipeline-blocking-example2 [n workers]
  (let [cin (a/to-chan! (range n))
        cout (a/chan)
        add-10-xf (map (partial (slow 100 +) 10))]
    (time2 (a/pipeline-blocking workers cout add-10-xf cin)
           (a/<!! (a/into [] cout)))))

(Duration/ofMillis 311)

(center
 (clerk/table
  (for [job [{:n 10 :workers 4}
             {:n 20 :workers 3}]]
    (let [{:keys [n workers]} job
          duration (:duration (pipeline-blocking-example2 n workers))]
      {:n n
       :workers workers
       :duration (format "%,6d ms" (.toMillis duration))}))))

(def duration-ms-viewer-wip
  {:transform-fn (nextjournal.clerk.viewer/update-val
                  (fn [duration]
                    (format "%,6d ms" (.toMillis duration))))})

(clerk/table
 (for [{:keys [n workers duration]}
       [{:n 10 :workers 4 :duration (Duration/ofMillis 311)}
        {:n 20 :workers 3 :duration (Duration/ofMillis 721)}
        {:n 500 :workers 50 :duration (Duration/ofMillis 1051)}]]
   {:n n
    :workers workers
    :duration (clerk/with-viewer duration-ms-viewer-wip duration)}))

nextjournal.clerk.viewer/default-viewers
nextjournal.clerk.viewer/add-viewers


^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
#_ (clerk/halt!)
