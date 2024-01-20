;; # A Playwright example
;;
;; In this document, I'll explore:
;;
;; - Java interop
;; - Using Playwright with the Java client.

(ns learn.playwright-example
  (:import (com.microsoft.playwright Playwright)
           com.microsoft.playwright.BrowserType$LaunchOptions))

;; Running this takes quite a few seconds!

(defonce playwright-state (atom nil)) ;; so we can (.close @playwright-state)

(defn init! []
  (when (nil? @playwright-state)
    (reset! playwright-state (Playwright/create))))

(init!)

(defn halt! []
  (when-let [p @playwright-state]
    (.close p)
    (reset! playwright-state nil)))

(comment
  (init!)
  (halt!)

  (do
    (def playwright @playwright-state)
    (def firefox
      (-> playwright
          (.firefox)
          (.launch
           (-> (BrowserType$LaunchOptions.)
               (.setHeadless false)))))

    (def page-firefox (.newPage firefox))
    (.navigate page-firefox "https://www.mikrobloggeriet.no/"))

  (do
    (def chromium
      (-> playwright
          (.chromium)
          (.launch
           (-> (BrowserType$LaunchOptions.)
               (.setHeadless false)))))
    (def page-chromium (.newPage chromium))
    (.navigate page-chromium "https://www.mikrobloggeriet.no/"))

  (do
    (def chrome
      (-> playwright
          (.chrome)
          (.launch
           (-> (BrowserType$LaunchOptions.)
               (.setHeadless false)))))
    (def page-chromium (.newPage chromium))
    (.navigate page-chromium "https://www.mikrobloggeriet.no/"))

  (.navigate page-chromium "https://www.mikrobloggeriet.no/")

  (defn navboth [url]
    (.navigate page-firefox url)
    (.navigate page-chromium url))

  (navboth "https://mikrobloggeriet.no")

  (let [domains ["https://iterate.no"
                 "https://vake.ai"
                 "https://mikrobloggeriet.no"]]
    (dotimes [_n 10]
      (navboth (rand-nth domains))
      (Thread/sleep 1000)))

  (halt!)

  :rcf)
