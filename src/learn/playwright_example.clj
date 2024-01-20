;; # A Playwright example
;;
;; In this document, I'll explore:
;;
;; - Java interop
;; - Using Playwright with the Java client.

(ns learn.playwright-example
  (:import [com.microsoft.playwright Playwright])
  (:require
   [nextjournal.clerk :as clerk]))

;; Running this takes quite a few seconds!
(def playwright (Playwright/create))
(def browser (.launch (.webkit playwright)))
(def page (.newPage browser))

page



(comment

  123

  (clerk/halt!)

  :rcf)
