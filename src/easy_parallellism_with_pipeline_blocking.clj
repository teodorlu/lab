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
;; Also, `clojure.core/time` outputs to standard out, so we'll make our own function for timing.

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
;; If one op takes about 100 ms, shouldn't three ops take about 300 ms?

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
 (clerk/table (for [n [3 5 10 20 50]]
                (let [speedup (calculate-pmapv-speedup (slow 10 inc) (repeatedly n rand))]
                  {"n" n
                   "speedup" (format "%.2f" speedup)
                   "speedup/n" (format "%.2f" (/ speedup n))}))))

;; On Teodor's personal computer, speedup/n drops from about 1 for n=5 to about 0.5 for n=20.
;; Speedup dropoff can be caused by us reaching `pmap`'s parallellisation limits, or because coordination overhead increases relative to compute time.

;; For a different perspective, let's look at slower operations (40 ms per op) for lower values of n.

(clerk/caption
 "Speedup for certain values of n"
 (clerk/table (for [n [2 3 4 5 6 7 8 12 15 18]]
                (let [speedup (calculate-pmapv-speedup (slow 40 inc) (repeatedly n rand))]
                  {"n" n
                   "speedup" (format "%.2f" speedup)
                   "speedup/n" (format "%.2f" (/ speedup n))}))))

;; [A Stackoverflow answer](https://stackoverflow.com/questions/5021788/how-many-threads-does-clojures-pmap-function-spawn-for-url-fetching-operations)
;; points towards a value derived from the number of processors on the system:

(+ 2 (.. Runtime getRuntime availableProcessors))

;; ## User-controlled parallellism with `clojure.core.async/pipeline-blocking`

;; In Clojure, we can utilize the core/async library for concurrency and
;; parallelism, lets explore what that looks like

;; Create a worker function, which increments values on the cin channel using slow+ and outputs the results
;; on the cout channel
(defn worker [cin cout]
  (a/go-loop []
    (when-some [val (a/<! cin)]
      (a/>! cout (slow+ 10 val))
      (recur))))

;; Run the function with the defined channels and collect the result
(let [cin (a/to-chan! (range 10))
      ;; We give the out channel a buffer for convenience sake. It allows us to
      ;; easily collect the results in a list.
      cout (a/chan 20)]
  (time
   ;; Wait for processing to finish. This works because go-blocks returns a
   ;; channel that receive a single value when the go-block is done
   (a/<!! (worker cin cout))
   (a/close! cout)
   (a/<!! (a/into [] cout))))

;; But this still only runs on one thread, we dont have any parallelism yet. To
;; get some parallelism, we need to spawn more worker functions.

(let [cin (a/to-chan! (range 10))
      cout (a/chan 20)]
  (time (let [worker-chns (mapv (fn [_] (worker cin cout)) (range 3))]
          ;; Wait for all channels to complete
          (mapv a/<!! worker-chns)
          (a/close! cout)
          (a/<!! (a/into [] cout)))))

;; There we go! Much better.

;; Managing and creating these worker functions, and correctly handling channels
;; and when to close them, can become a bit tricky. Luckily, core/async provides
;; an awesome helper function in the form of pipeline-blocking.

;; pipeline-blocking works on an input and an output channel, a number
;; representing the number of concurrent workers and a transducer to apply to
;; the values flowing between the channels. Transducers are a feature in Clojure
;; that has great interop with core/async. We can connect transducers to
;; channels when we create a channel via the (async/chan buf xf) function, or we can
;; use other functions such as pipeline-blocking to get a bit more control over
;; parallel execution.

;; Rewriting the above code to use pipeline-blocking:
;; First we need to define a transducer
(def xf (map slow+))

;; Then we setup some channels and run pipeline-blocking
(let [cin (a/to-chan! (range 10))
      cout (a/chan 20)]
  (time (a/pipeline-blocking 3 cout xf cin)
        (a/<!! (a/into [] cout))))

;; Lets see if we can push it a bit...
(let [cin (a/to-chan! (range 500))
      cout (a/chan 600)]
  (time (a/pipeline-blocking 50 cout xf cin)
        (a/<!! (a/into [] cout))))

;; ...and thats it!
;; In my view, pipeline-blocking offers a lot of benefits. We dont have to manage worker functions, and
;; we can utilize transducers, which can be reused in many different contexts without needing knowledge
;; of the underlying datastructure.

;; Sometimes, we do need the extra control that custom worker functions provide, but I suspect that
;; pipeline-blocking is sufficient in manye scenarios.

;; You might have noticed that there are a couple of other pipeline-related
;; functions, namely (pipeline-async) and (pipeline). The difference between these is
;; that pipeline-blocking uses (async/thread) under the hood, while
;; pipeline-async and pipeline uses (async/go). You can see that in action here:
;; https://github.com/clojure/core.async/blob/aa6b951301fbdcf5a13cdaaecb4b1a908dc8a978/src/main/clojure/clojure/core/async.clj#L548
;; The names of the functions suggest the proper usage: if you have any blocking
;; operations such as network calls or in our case, the (slow+) function, then
;; use pipeline-blocking. If you have async operations, use pipeline-async. If
;; you are doing compute-intensive operations, use pipeline.

;; The reason for differentiating between these functions is because of a common
;; pitfall when using go-blocks: blocking operations will starve the thread pool
;; (which has a default of 8 threads). Unlike in Golang where goroutines are
;; preempted on I/O calls and other events, this is not the case in Clojure. Go
;; blocks in Clojure are only preempted when we perform a take or put on a
;; channel. This means that a go-block performing a blocking network call will
;; block that thread until the network call is done, if enough of these are
;; running at the same time the core/async thread pool for go blocks will run
;; out of threads.

;; The ideal solution to such a situation is to make the network call
;; asynchronous so that we can still use go-blocks without starving the thread
;; pool, but a much simpler solution is to spin up dedicated threads with
;; (async/thread). (async/go) and (async/thread) blocks are pretty much
;; interchangeble with the exception of some core/async functions which have
;; their own versions, such as (async/<!!) for (async/thread) blocks and
;; (async/<!) for (async/go) blocks. Unless a very large number of threads are
;; needed, (async/thread) and pipeline-blocking works perfectly fine. If you
;; need several hundreds of threads, then it might be good idea to consider
;; pipeline-async.

;; This is a good blog post that explores the topic a bit more:
;; https://eli.thegreenplace.net/2017/clojure-concurrency-and-blocking-with-coreasync/

#_(clerk/clear-cache!)
