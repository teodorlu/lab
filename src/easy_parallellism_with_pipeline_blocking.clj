;; # Easy parallelism with `pipeline-blocking`
;;
;; If you want a `map` to go faster, you could use `pmap`.
;; But what if you need more control than `pmap` can give you?
;; What if your problem is memory constrained?
;; Read on to discover your options!

;; `pipeline-blocking` is from `clojure.core.async`.
;; Otherwise, we're just going to use plain Clojure functions.

(ns easy-parallellism-with-pipeline-blocking
  (:refer-clojure :exclude [time])
  (:require
   [clojure.core.async :as a]
   [nextjournal.clerk :as clerk]))

;; ## A slow operation and a timer
;;
;; We're going to explore our parallelism interactively.
;; For this we'll need two helpers, a slow operation, and a way to time operations.
;; First, a slow operatoin.
;;
;; Also, `clojure.core/time` outputs to standard out, so we'll make our own function for timing.`

(do
  (defn slow [extra-latency-ms op]
    (fn [& args]
      (Thread/sleep extra-latency-ms)
      (apply op args)))

  (def slow+ (slow 100 +))

  (slow+ 1 2))

;; It returns 3!
;; But you, the reader have no reason to know wether it was actually slow or not.

(defn time* [f]
  (let [before (System/currentTimeMillis)
        value (f)
        after (System/currentTimeMillis)
        duration-millis (- after before)]
    {:value value :duration-millis duration-millis}))

(defmacro time
  "A `clojure.core/time` alternative suitable for notebook usage"
  [expr]
  `(time* (fn [] ~expr)))

(time* #(slow+ 1 2))

(macroexpand-1
 '(time (slow+ 1 2)))

(time (slow+ 1 2))

#_(clerk/clear-cache!)
