;; # A Mermaid.js example

(ns mermaid-example
  (:require
    [nextjournal.clerk :as clerk]))

;; Based on an example from the Clerk book:
;;
;; https://book.clerk.vision/#loading-libraries

(def mermaid-viewer
  {:transform-fn clerk/mark-presented
   :render-fn '(fn [value]
                 (when value
                   [nextjournal.clerk.render/with-d3-require {:package ["mermaid@8.14/dist/mermaid.js"]}
                    (fn [mermaid]
                      [:div {:ref (fn [el] (when el
                                             (.render mermaid (str (gensym)) value #(set! (.-innerHTML el) %))))}])]))})

(clerk/with-viewer mermaid-viewer
  "stateDiagram-v2
    [*] --> Still")

(clerk/with-viewer mermaid-viewer
  "flowchart LR
    source[\"(range 3)\"]-- cin -->worker-- cout -->sink[\"[10, 11, 12]\"]")
