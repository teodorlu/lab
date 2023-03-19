(ns arrows
  (:require
   [nextjournal.clerk :as clerk]
   [arrowic.core :as arrowic]))

{::clerk/visibility {:code :fold}}

(clerk/html "hi")
(clerk/html "there")

(-> (clerk/html (slurp "/home/teodorlu/dev/jackrusher/arrowic/ex2.svg"))
    #_
    (assoc :nextjournal/width :full))
