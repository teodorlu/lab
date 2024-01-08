(ns repro.katex-teodor-version
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as viewer]))

(def katex-inline-viewer
  (update viewer/katex-viewer :transform-fn comp (fn [wv] (-> wv (assoc-in [:nextjournal/render-opts :inline?] true)))))

(defn tex [x]
  (clerk/with-viewer katex-inline-viewer x))

{:a (tex "\\phi") :b (tex "\\psi")}
