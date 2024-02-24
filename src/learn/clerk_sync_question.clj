(ns learn.clerk-sync-question
  (:require
   [nextjournal.clerk :as clerk]))

(defn process-vthread [concurrency result f coll]
  (let [semaphore (java.util.concurrent.Semaphore. concurrency)
        latch (java.util.concurrent.CountDownLatch. (count coll))
        p (promise)]

    ;; start én virtual thread per element
    (->> coll
         (run! #(Thread/startVirtualThread
                 (fn []
                   (try
                     (.acquire semaphore)
                     (f % result)
                     (finally
                       (.countDown latch)
                       (.release semaphore)))))))

    ;; start en virtual thread som venter på alle jobbene og sier "ferdig"
    (Thread/startVirtualThread
     (fn []
       (.await latch)
       (deliver p :done)))

    ;; returner promise, som blir levert når alt er klart
    p))

^{::clerk/sync true
  ::clerk/visibility {:result :hide}}
(defonce status2 (atom {}))
