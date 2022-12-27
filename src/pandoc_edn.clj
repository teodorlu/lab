(ns pandoc-edn
  (:require [teodorlu.pandoc.alpha2 :as pandoc]))

pandoc/!runner

(comment
  ;; perhaps

  pandoc/!binary-fn
  pandoc/!exec
  pandoc/!exec-fn

  ;; I think I like !exec-fn
  )

(def pandoc (constantly nil))
