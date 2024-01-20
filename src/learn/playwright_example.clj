;; # A Playwright example
;;
;; In this document, I'll explore:
;;
;; - Java interop
;; - Using Playwright with the Java client.

(ns learn.playwright-example
  (:import (com.microsoft.playwright Playwright Page BrowserType
                                     Browsertype$LaunchOptions
                                     ))
  (:require
   [nextjournal.clerk :as clerk]))

;; Running this takes quite a few seconds!
(defonce playwright-state (atom (Playwright/create))) ;; so we can (.close @playwright-state)
(def playwright @playwright-state)

(import com.microsoft.playwright.BrowserType$LaunchOptions)
(-> playwright
    (.firefox)
    (.launch
     (-> (BrowserType$LaunchOptions.)
         (.setHeadless false)
         (.setSlowMo 50))))
;; => "Unable to resolve classname: BrowserType$LaunchOptions"


;; (def browser (.launch (.webkit playwright)))
;; (def page (.newPage browser))



(comment

  123

  (clerk/halt!)

  :rcf)

(comment
  ;; eksperimentering teodor

  com.microsoft.playwright.BrowserType$LaunchOptions
  ;; => com.microsoft.playwright.BrowserType$LaunchOptions

  ;; Dette ser lovende ut!!!
  )
