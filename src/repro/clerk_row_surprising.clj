(ns repro.clerk-row-surprising
  (:require [nextjournal.clerk :as clerk]))

(def ^:private geometry-parameters
  #{:h :b :s :t :r})

(defn scale-by
  "Shrink or expand a profile with a factor"
  [profile f & args]
  (->> profile
       (filter (comp geometry-parameters key))
       (map (fn [[param value]]
              [param (apply f value args)]))
       (into {})))

(defn scale
  "Shrink or expand a profile with a factor"
  [profile factor]
  (->> profile
       (filter (comp geometry-parameters key))
       (map (fn [[param value]]
              [param (* value factor)]))
       (into {})))

(def ^:private ipe300
  {:r 15, :wy 557, :s 7.1, :prefix "IPE", :wz 80.5, :h 300, :b 150, :iz 6.04, :t 10.7, :iy 83.6, :profile 300, :a 5.38})

(def ^:private hea300
  {:r 27, :wy 1260, :s 8.5, :prefix "HEA", :wz 421, :h 290, :b 300, :iz 63.1, :t 14, :iy 182.6, :profile 300, :a 11.2})

;; This looks like a bug to me.
;; Only ipe300 is shown.
;; Perhaps I'm using it wrong.

(clerk/row
 (scale ipe300 0.5)
 (scale hea300 0.5))

;; The list viewer shows both:

(list
 (scale-by ipe300 * 0.5)
 (scale-by hea300 * 0.5))

;; If both elements are wrapped in a vector, both are shown.

(clerk/row
 [(scale ipe300 0.5)
  (scale hea300 0.5)])

(clerk/row {:item 1} {:item 2})

(clerk/row [{:item 1} {:item 2}])

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
