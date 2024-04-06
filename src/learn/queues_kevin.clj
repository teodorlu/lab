(ns learn.queues-kevin
  (:require
   [clojure.core.async :as a]))

(comment
  (def q (a/chan))

  (a/go (a/>! q "hei")
        (a/>! q "hei 2"))

  (a/<!! q)

  )
