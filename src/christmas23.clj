(ns christmas23
  (:require
   [clojure.string :as str]
   [nextjournal.clerk :as clerk]))

(defn transform [& xs] {:transform (str/join " " xs)})
(defn translate [cx cy] (str "translate(" cx " " cy ")"))

(defn svg* [& xs]
  (let [w 600
        h 300]
    [:svg {:width w :height h}
     (into
      [:g (transform (translate (/ w 2) (/ h 2)))]
      xs)]))

(defn svg [& xs] (clerk/html (apply svg* xs)))
(defn hsl [h s l] (str "hsl(" h ", " s "%, " l "%)"))

(def red (hsl 8 80 60))

(svg [:circle {:cx 0 :cy 0 :r 25 :fill red}])

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
