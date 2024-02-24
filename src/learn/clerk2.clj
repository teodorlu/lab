;; # Clojure concurrency howto
;;
;; Problem statement: get hands-on experience with different approaches to doing
;; concurrency with Clojure, then compare how those approaches feel in practice.

(ns learn.clerk2
  (:require
   [nextjournal.clerk :as clerk]))

;; ## Just enough dataviz to see what we're doing

(def status->color {:wait '🔵 :in-progress '🟠 :done '🟢})
(mapv status->color [:wait :wait :in-progress :in-progress :done :done :done])

;; ## Jobs and workers

^{::clerk/sync true
  ::clerk/visibility {:result :hide}}
(defonce jobs (atom []))

(mapv status->color @jobs)

(comment
  (reset! jobs [])
  (dotimes [_i 10] (swap! jobs conj :wait))

  :rcf)

;; ## What now?
;;
;; - We have a way to view status reactively.
;;   It updates once we change it.
;;
;; - I asked about concurrency earlier on Slack:
;;   https://clojurians.slack.com/archives/C061XGG1W/p1707477511195369

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

@status2

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
