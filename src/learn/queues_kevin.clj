(ns learn.queues-kevin
  (:require
   [clojure.core.async :as a]))

::q
;; => :learn.queues-kevin/q


(comment
  (def q (a/chan))

  (a/go (a/>! q "hei")
        (a/>! q "hei 2"))
  ;; "spawner" noe på en grønn tråd som skriver verdier først når de er klare til å leses

  (a/<!! q)
  ;; leser en verdi
  ;; kjør den to ganger for å lese både "hei" og "hei 2".

  )

(defn provide
  "Puts two values on the queue"
  [ctx]
  (a/go
    (a/>! (::q ctx) "Hello!")
    (a/>! (::q ctx) "Hello, there!!!")))

(defn consume-values
  "Reads two values from the queue"
  [ctx]
  [(a/<!! (::q ctx))
   (a/<!! (::q ctx))])

(comment
  (def ctx {::q (a/chan)})

  (provide ctx)

  (consume-values ctx)
  ;; => ["Hello!" "Hello, there!!!"]

  (time (let [ctx2 {::q (a/chan)}]
          (provide ctx2)
          (consume-values ctx2)))

  (let [ctx2 {::q (a/chan)}]
    (time (do
            (provide ctx2)
            (consume-values ctx2))))

  )

(defn provide2
  "Puts two values on the queue"
  [ctx n]
  (dotimes [_ n]
    (a/go (a/>! (::q ctx) (rand)))))

(defn consume2+add
  [ctx n]
  (repeatedly n #(a/<!! (::q ctx))))

(comment
  (let [ctx {::q (a/chan)}]
    (a/go (a/>! (::q ctx) 42))
    (a/<!! (::q ctx)))

  (let [ctx {::q (a/chan)}]
    (a/go (provide2 ctx 1))
    (a/<!! (::q ctx)))

  (def one-hundred-thousand 100000)
  (def thousand 1000)

  (let [n (* 10 thousand)
        q (a/chan)]
    (time (do
            (dotimes [_ n]
              (a/go (a/>! q 0)))
            (dotimes [_ n]
              (a/<!! q)))))

  (dotimes [_ 20]
    (let [n (* 100 thousand)
          arr (make-array Double/TYPE n)]
      (time
       (do
         (dotimes [i n]
           (aset arr i 4.2))
         (dotimes [i n]
           (aget arr i))
         :done))))

  (time (dotimes [_ 100000] rand))

  :rcf)
