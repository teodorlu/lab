(ns learn.java-interop)

;; reading https://clojure-doc.org/articles/language/interop/

(import java.awt.Point)

;; Java field access

(let [p (Point. 0 10)]
  (.x p))
;; => 0

;; Setters

(let [p (Point. 0 10)]
  (set! (.-y p) 100)
  (.y p))
;; => 100

;; Can use both (.x p) and (.-x p=)

(let [p (Point. 0 10)]
  (set! (.-y p) 100)
  [(.y p) (.-y p)])
;; => [100 100]

(= java.util.Date
   (Class/forName "java.util.Date"))
;; => true
