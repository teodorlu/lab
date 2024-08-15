(ns learn.liberator
  (:require
   [nextjournal.clerk :as clerk]))

(let [targets ["decisions" "actions" "handlers"]
      target-href (fn [target] (format "http://clojure-liberator.github.io/liberator/doc/%s.html" target))]
  (clerk/row (for [target targets]
               (clerk/html [:a {:href (target-href target)}
                            (str "Liberator " target)])) ))
