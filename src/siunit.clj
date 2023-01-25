(ns siunit
  (:refer-clojure :exclude [+ * /])
  (:require [teodorlu.siunit.alpha1 :as siunit :refer [+ * /]]
            [teodorlu.siunit.alpha1.metric-prefix :refer [k m]]))

(def N {:kg 1 :m 1 :s -2})

;; Since we coerce into SI Units, we can do funny stuff like this:

(* 12 k N :m
   30 m :m)

;; Or be more explicit:

(let [force (* 12 k N :m)
      length (* 30 m :m)]
  (* force length))

;; Let's ask for that number in kN.
(let [force (* 12 k N)
      length (* 30 m :m)]
  (/
   (* force length)
   k N))

;; There's also a reader literal:

#siunit [30 :kg]

;; The reader literal interprets keywords as SI base units, numbers as unitless numbers and maps as unit exponent maps.

#siunit :m

#siunit 20

#siunit {:m 2}

;; The vector form multiplies its arguments.

#siunit [30 :kg {:m -2}]

;; To update Clerk Garden: https://github.clerk.garden/teodorlu/clerk-stuff?update=1
