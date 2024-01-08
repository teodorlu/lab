(ns repro.katex-andrea-version
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as viewer]))

^{::clerk/visibility {:result :hide}}
(clerk/add-viewers! [(update viewer/katex-viewer
                             :transform-fn comp (fn [wv] (-> wv (assoc-in [:nextjournal/render-opts :inline?] true))))])

{:a (clerk/tex "\\phi") :b (clerk/tex "\\psi")}
