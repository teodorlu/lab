;; # Clojure concurrency howto
;;
;; Problem statement: get hands-on experience with different approaches to doing
;; concurrency with Clojure, then compare how those approaches feel in practice.

(ns learn.clojure-concurrency
  (:require [nextjournal.clerk :as clerk]))

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

(defn process-vthread [concurrency started-fn process-fn done-fn jobs]
  (let [semaphore (java.util.concurrent.Semaphore. concurrency)
        latch (java.util.concurrent.CountDownLatch. (count jobs))
        p (promise)]

    ;; start én virtual thread per element
    (->> jobs
         (run! #(Thread/startVirtualThread
                 (fn []
                   (try
                     (.acquire semaphore)
                     (started-fn %)
                     (let [result (process-fn %)]
                       (prn [:done % result])
                       (done-fn % result))
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

(defn status2-initial-state [job-count]
  (into {}
        (for [i (range job-count)]
          [(keyword (str "j" i)) :wait])))

(update-vals @status2 status->color)

^{::clerk/visibility {:code :hide :result :hide}}
(comment
  ;; first, assign work
  (reset! status2 (status2-initial-state 8))

  @status2

  (def all-done-promise
    (let [started-fn (fn [job] (swap! status2 assoc job :started))
          process-fn (fn [job]
                       (swap! status2 assoc job :in-progress)
                       (slurp "https://teod.eu")
                       :done)
          done-fn (fn [job _result] (swap! status2 assoc job :done))
          jobs (keys @status2)]
      (process-vthread 3 started-fn process-fn done-fn jobs)))

  all-done-promise

  *clojure-version*
  (System/getProperty "java.version")
  )

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
