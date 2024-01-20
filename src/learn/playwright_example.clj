;; # A Playwright example
;;
;; In this document, I'll explore:
;;
;; - Java interop
;; - Using Playwright with the Java client.

(ns learn.playwright-example
  (:import (com.microsoft.playwright Playwright Page BrowserType)
           com.microsoft.playwright.BrowserType$LaunchOptions)
  (:require
   [nextjournal.clerk :as clerk]
   [clojure.inspector :as inspector]
   [clojure.java.data :as j]))

;; Running this takes quite a few seconds!

(defonce playwright-state (atom nil)) ;; so we can (.close @playwright-state)
(when [(not= nil @playwright-state)]
  (reset! playwright-state (Playwright/create)))

(def playwright @playwright-state)

(def firefox
  (-> playwright
      (.firefox)
      (.launch
       (-> (BrowserType$LaunchOptions.)
           (.setHeadless false)
           #_(.setSlowMo 50)))))

(def page (.newPage firefox))
(.navigate page "https://clojure.org")


(comment
  (j/from-java page)

  )

(comment

  123

  (clerk/halt!)

  (when-let [p @playwright-state]
    (.close p)
    (reset! playwright-state nil))

  :rcf)

(comment
  ;; eksperimentering teodor

  com.microsoft.playwright.BrowserType$LaunchOptions
  ;; => com.microsoft.playwright.BrowserType$LaunchOptions

  ;; Dette ser lovende ut!!!
  )
