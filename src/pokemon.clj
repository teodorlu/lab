(ns pokemon
  (:require
   [nextjournal.clerk :as clerk]))

;; based on / inspired by ladymey's Tablecloth talk:
;;
;; https://www.youtube.com/watch?v=a0T_d_N7wbg / https://github.com/ladymeyy/tablecloth-talks-and-workshop

;; <iframe width="560" height="315" src="https://www.youtube.com/embed/a0T_d_N7wbg" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>

(defn youtube-embed [youtube-id]
  (let [default-size {:width 560 :height 315}
        teodor-preferred-size (let [w 756]
                                {:width w :height (* (/ w (:width default-size)) (:height default-size))})]
    [:iframe (merge teodor-preferred-size
                    {:src (str "https://www.youtube.com/embed/" youtube-id)
                     :title "YouTube video player" :frameborder 0
                     :allow "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                     :allowfullscreen true})]))

(clerk/html (youtube-embed "a0T_d_N7wbg"))
