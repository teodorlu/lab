(ns learn.ggplot-idea
  (:refer-clojure :exclude [+]))

;; Idea: translate ggplot code mindlessly to records and protocols.
;;
;; Source: https://r4ds.hadley.nz/data-visualize
;;
;; ```r
;; ggplot(
;;   data = penguins,
;;   mapping = aes(x = flipper_length_mm, y = body_mass_g)
;; )
;; ```

(defprotocol IMapping
  "A GGPlot mapping"
  (mapping [this]))

(defrecord Aes [x y]
  "A GGPlot aestetic"
  IMapping
  (mapping [this] :TODO))

(defn aes [& args] :TODO)

(defn + [ggplot component] :TODO)

(defprotocol IComponent
  (component [this]))

(defrecord GeomPoint []
  IComponent
  (component [this] :TODO))

(defrecord GGPlot [data mapping environment])

(defn ggplot [& args] :TODO)
(def penguins :TODO)

(comment

  (ggplot :data penguins
          :mapping (aes :x :flipper_length_mm :y :body_mass_g))

  )
