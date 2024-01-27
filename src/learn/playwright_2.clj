(ns learn.playwright-2
  (:import (com.microsoft.playwright Playwright BrowserType$LaunchOptions))
  (:require
    [clojure.string :as str]
    [nextjournal.clerk :as clerk]))

(defonce playwright (Playwright/create))

(defn firefox! [pw]
  (-> pw
      (.firefox)
      (.launch
       (-> (BrowserType$LaunchOptions.)
           (.setHeadless false)))))

(def firefox (firefox! playwright))

(comment
  (.close firefox)
  (alter-var-root #'firefox (constantly (firefox! playwright)))
  )

;; are pages alive?

(def urls
  (for [name ["mikrobloggeriet.no"
              "play.teod.eu"
              "teod.eu"
              "teod.eu/aphorisms"]]
    {:filename (str (str/replace name #"/" "-") ".png")
     :url (str "https://" name)}))

(clerk/table urls)

(comment
  (doseq [url urls]
    (let [page (.newPage firefox)]
      (.navigate page url))
    )

  )
