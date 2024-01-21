(ns learn.mikrobloggeriet-css-bug
  (:import (com.microsoft.playwright Playwright BrowserType$LaunchOptions)))

;; reproduce https://github.com/iterate/mikrobloggeriet/issues/79 locally

(def m-local "http://localhost:7223")
(def m-prod "https://mikrobloggeriet.no")

(defonce playwright-instance (atom nil))
(defn init! []
  (when (nil? @playwright-instance)
    (reset! playwright-instance (Playwright/create))))

(defn halt! []
  (when-not (nil? @playwright-instance)
    (.close @playwright-instance)
    (reset! playwright-instance nil)))

(comment
  @playwright-instance

  (init!)
  (halt!)
  )

(defn init-pages [playwright]
  (let [browser-opts (-> (BrowserType$LaunchOptions.)
                         (.setHeadless false))
        browser-templates [(.firefox playwright)
                           (.chromium playwright)
                           (.webkit playwright)
                           ;; playwright does not have "safari", but webkit is similar
                           ]
        ]
    ;; return a browser _page_ for each (a tab)
    (doall
     (for [template browser-templates]
       (.newPage (.launch template browser-opts))))))

(defonce pages (atom nil))

(comment
  ;; start playwright
  (init!)
  ;; start a bunch of pages
  (reset! pages (init-pages @playwright-instance))
  (first @pages)

  (def m-local "http://localhost:7223")
  (def m-prod "https://mikrobloggeriet.no")
  (def m-olorm-1 "https://mikrobloggeriet.no/olorm/olorm-1/")

  (doseq [p @pages]
    (.navigate p (str m-prod "/o/olorm-23/")))

  (doseq [p @pages]
    (.navigate p (str m-local "/o/olorm-48/")))

  (doseq [p @pages]
    (.navigate p (str m-local "/luke/luke-7/")))

  )
