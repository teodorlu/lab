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
  (when [(not= nil @playwright-state)]
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
    (.navigate page-firefox "https://www.teod.eu/"))

  (do
    (def chromium
      (-> playwright
          (.chromium)
          (.launch
           (-> (BrowserType$LaunchOptions.)
               (.setHeadless false)))))
    (def page-chromium (.newPage chromium))
    (.navigate page-chromium "https://www.ao.no/"))

  (.navigate page-chromium "https://www.nettavisen.no/")

  (defn navboth [url]
    (.navigate page-firefox url)
    (.navigate page-chromium url))

  (navboth "https://iterate.no")

  (let [domains ["https://www.evalapply.org"
                 "https://clojure.org"
                 "https://teod.eu"]]
    (dotimes [_n 10]
      (.navigate page-firefox (rand-nth domains))
      (Thread/sleep 1000)))

  (halt!)

  :rcf)
