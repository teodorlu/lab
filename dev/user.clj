(ns user
  (:require [nextjournal.clerk :as clerk]))

(def ^:private clerk-port
  7998)

(comment
  ;; start without file watcher, open browser when started
  (clerk/serve! {:browse? true :port clerk-port})

  (clerk/clear-cache!)

  )
