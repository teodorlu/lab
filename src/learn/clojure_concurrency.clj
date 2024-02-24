;; # Clojure concurrency howto
;;
;; Problem statement: get hands-on experience with different approaches to doing
;; concurrency with Clojure, then compare how those approaches feel in practice.

(ns learn.clojure-concurrency
  (:require [nextjournal.clerk :as clerk]))

^::clerk/sync
(defonce state1 (atom {}))

@state1

(comment
  (swap! state1 update :counter (fnil inc 0))
  )
