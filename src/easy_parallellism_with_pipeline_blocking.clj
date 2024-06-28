;; # Easy, explicit parallellism with `pipeline-blocking`
;;
;; If you want a `map` to go faster, you could use `pmap`.
;; But what if you need more control than `pmap` can give you?
;; What if your problem is memory constrained?
;; Read on to discover your options!


(ns easy-parallellism-with-pipeline-blocking
  {:nextjournal.clerk/toc true}
  (:refer-clojure :exclude [time])
  (:require
   [clojure.core.async :as a]
   [nextjournal.clerk :as clerk]))

;; `pipeline-blocking` is from `clojure.core.async`.
;; And we're going to make our own way to time our code.

;; ## A slow operation and a timer
;;
;; We're going to explore our parallelism interactively.
;; For this we'll need two helpers, a slow operation, and a way to time operations.
;; First, a slow operation.
;;
;; Also, `clojure.core/time` outputs to standard out, so we'll make our own function for timing.`

{:nextjournal.clerk/visibility {:result :hide}}

(defn slow [extra-latency-ms op]
  (fn [& args]
    (Thread/sleep extra-latency-ms)
    (apply op args)))

(def slow+ (slow 100 +))

{:nextjournal.clerk/visibility {:result :show}}

(slow+ 1 2)

;; It returns 3!
;; But you, the reader have no reason to know wether it was actually slow or not.
;; If one op takes about 100 ms, shouldn't three ops take about 300 ms?

{:nextjournal.clerk/visibility {:result :hide}}

(defn time* [f]
  (let [before (System/currentTimeMillis)
        value (f)
        after (System/currentTimeMillis)
        duration-millis (- after before)]
    {:value value :duration-millis duration-millis}))

(defmacro time
  "A `clojure.core/time` alternative suitable for notebook usage"
  [& body]
  `(time* (fn [] ~@body)))

{:nextjournal.clerk/visibility {:result :show}}

(time (slow+ 1 2))

;; There you go.
;; Now, you, the reader, might assume that what we wrote after `:duration-millis` was how long time it actually took.
;; If you decide to put your faith in us today.

;; ## Single threaded with `clojure.core/map`

(time (map (partial slow+ 10) [100 200 300]))

;; Wait, 0 ms?
;; That was not what we exected.

;; `(doc map)` gives a clue:
;;
;;     clojure.core/map
;;     ([f] [f coll] [f c1 c2] [f c1 c2 c3] [f c1 c2 c3 & colls])
;;       Returns a lazy sequence consisting of the result of applying f to
;;     […]
;;
;; Lazyness!
;; Right.
;; "Lazyness and impurity is a bad match", timing impure.
;; We returned a lazy sequence that did no work, then timed no work, then the rendering of the value triggered the realization of the lazy sequence.
;; We could force the lazyness out with a doall in the expression:

(time (doall (map (partial slow+ 10) [100 200 300])))

;; Or avoid the issue with `clojure.core/mapv`, which is eager.

(time (mapv (partial slow+ 10) [100 200 300]))

;; ## Runtime-controlled parallelism with `clojure.core/pmap`

(time (pmap (partial slow+ 10) [100 200 300]))

;; Lazyness gives us yet another smack!

;; `pmapv` isn't provided, so let's create it.

^{:nextjournal.clerk/visibility {:result :hide}}
(def pmapv (comp vec pmap))

(time (pmapv (partial slow+ 10) [100 200 300]))

;; There you go!
;; `pmap` sometimes makes your code go faster.
;; Sometimes that's just great, and exactly what you need.

;; To figure out how much parallelism Clojure decides to use, we _can_ read the manual—but we can also fiddle with the REPL.
;; We're going to work with a function that takes about 10 ms instead of about 100 ms to save some time.

(def timing-1
  (time (mapv (slow 10 inc)
              [10 20 30])))

;; Great!
;; Now with parallelism:

(def timing-2
  (time (pmapv (slow 10 inc)
               [10 20 30])))

;; Speedup is _almost 3_ on _certain systems_:

(double
 (/ (:duration-millis timing-1)
    (:duration-millis timing-2)))

;; Let's plot n, speedup(n) and speedup(n)/n for some values of n.

(defn calculate-pmapv-speedup [op args]
  (double
   (/ (:duration-millis (time (mapv op args)))
      (:duration-millis (time (pmapv op args))))))

;; For our own sanity, can we reproduce something like the previous speedup?

(calculate-pmapv-speedup (slow 10 inc) [10 20 30])

;; Now, let's plot n and speedup for a few different values.

(clerk/caption
 "Speedup for certain values of n"
 (let [two-decimals #(format "%.2f" %)]
   (clerk/table (for [n [3 5 10 20 50]]
                  (let [speedup (calculate-pmapv-speedup (slow 10 inc) (repeatedly n rand))]
                    {"n" n
                     "speedup" (two-decimals speedup)
                     "speedup/n" (two-decimals (/ speedup n))})))))

;; On Teodor's personal computer, speedup/n drops from about 1 for n=5 to about 0.5 for n=20.
;; Speedup dropoff can be caused by us reaching `pmap`'s parallellisation limits, or because coordination overhead increases relative to compute time.

;; For a different perspective, let's look at slower operations (40 ms per op) for lower values of n.

(clerk/caption
 "Speedup for certain values of n"
 (let [two-decimals #(format "%.2f" %)]
   (clerk/table (for [n [2 3 4 5 6 7 8 12 15 18]]
                  (let [speedup (calculate-pmapv-speedup (slow 40 inc) (repeatedly n rand))]
                    {"n" n
                     "speedup" (two-decimals speedup)
                     "speedup/n" (two-decimals (/ speedup n))})))))

;; [A Stackoverflow answer](https://stackoverflow.com/questions/5021788/how-many-threads-does-clojures-pmap-function-spawn-for-url-fetching-operations)
;; points towards a value derived from the number of processors on the system:

(+ 2 (.. Runtime getRuntime availableProcessors))

;; ## User-controlled parallellism with `clojure.core.async/pipeline-blocking`

;; TODO

#_(clerk/clear-cache!)
