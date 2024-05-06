(ns learn.describe-example)

(def a-dog {:dog/breed "Standard Schnauzer"})
(def a-car {:car/make "Volvo"})
(def a-person {:person/name "Nik"})
(def examples [a-dog a-car a-person])

(defn example-type [example]
  (cond (:dog/breed example)
        :dog

        (:car/make example)
        :car))

(example-type a-dog)
;; => :dog

(example-type a-car)
;; => :car

(example-type a-person)
;; => nil

(def describe-example
  {:dog (fn [dog] (str "A dog with breed " (:dog/breed dog)))
   :car (fn [car] (str "A car with make " (:car/make car)))})
