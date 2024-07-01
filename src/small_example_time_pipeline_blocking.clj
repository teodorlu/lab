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

{:nextjournal.clerk/visibility {:result :show}}

;; examples:

(clerk/caption
 "n=20, 3 workers"
 (let [cin (a/to-chan! (range 20))
       cout (a/chan)
       add-10-xf (map (partial (slow 100 +) 10))]
   (time (a/pipeline-blocking 3 cout add-10-xf cin)
         (a/<!! (a/into [] cout)))))

(clerk/caption
 "n=500, 50 workers"
 (let [cin (a/to-chan! (range 500))
       cout (a/chan)
       add-10-xf (map (partial (slow 100 +) 10))]
   (time (a/pipeline-blocking 50 cout add-10-xf cin)
         (a/<!! (a/into [] cout)))))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(comment

  ;; (defn example [n workers]
  ;;   (let [cin (a/to-chan! (range n))
  ;;         cout (a/chan)
  ;;         add-10-xf (map (partial (slow 100 +) 10))]
  ;;     (time (a/pipeline-blocking workers cout add-10-xf cin)
  ;;           (a/<!! (a/into [] cout)))))



  ;; (clerk/table )
  )
