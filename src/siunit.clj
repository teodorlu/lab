(ns siunit
  (:refer-clojure :exclude [+ * /])
  (:require [teodorlu.siunit.alpha1 :as siunit :refer [+ * /]]
            [teodorlu.siunit.alpha1.metric-prefix :refer [k m]]))

;; In the spirit of SICMUtils, we try to make units work as you would want them with special operators.

;; Units are maps from base units to their exponent.

(def N {:kg 1 :m 1 :s -2})

;; We overload *, which means we can do something like this:

(* 12 k N :m
   30 m :m)

;; Or we can name arguments:

(let [force (* 12 k N :m)
      length (* 30 m :m)]
  (* force length))

;; Let's ask for that number in kN.

(let [force (* 12 k N)
      length (* 30 m :m)]
  (/
   (* force length)
   k N))

;; Summation is allowed when operands are of the same unit:

(+ (* 1 :m)
   (* 1 m :m))

;; but not when operands are of different units:

(comment
  (+ (* 1 :m)
     (* 1 :kg)))
;; 👆 throws an AssertionError

;; (I find :m as meter, and m as milli to be a bit confusing above)

;; There's also a reader literal:

#siunit [30 :kg]

;; The reader literal interprets keywords as SI base units, numbers as unitless numbers and maps as unit exponent maps.

#siunit :m

#siunit 20

#siunit {:m 2}

;; The vector form multiplies its arguments.

#siunit [30 :kg {:m -2}]

;; To update Clerk Garden: https://github.clerk.garden/teodorlu/clerk-stuff?update=1
