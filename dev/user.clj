(ns user
  (:require [nextjournal.clerk :as clerk]))

(def ^:private clerk-port
  7998)

^{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn clerk-start! [] (clerk/serve! {:browse? true :port clerk-port}))

(comment
  (clerk/clear-cache!)

  )
