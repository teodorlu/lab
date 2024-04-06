(ns learn.queues-kevin
  (:require
   [clojure.core.async :as a]))

(comment
  (def q (a/chan))

  (a/go (a/>! q "hei")
        (a/>! q "hei 2"))

  (a/<!! q)

  )

(defn provide-values
  "Puts two values on the queue"
  [ctx]
  (a/go
    (a/>! (::q ctx) "Hello!")
    (a/>! (::q ctx) "Hello, there!!!")))

(defn consume-values [ctx]
  [(a/<!! (::q ctx))
   (a/<!! (::q ctx))])

(comment
  (def ctx {::q (a/chan)})

  (provide-values ctx)

  (consume-values ctx)

  )
