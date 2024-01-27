;; # Using Playwright from Clojure
;;
;; In which,
;;
;; - You get a feel for what Playwright can do for you
;; - You get a feel for Playwright's Java API used through Clojure's Java interop.


(ns learn.playwright-2
  (:require
   [nextjournal.clerk :as clerk]
   nextjournal.clerk.analyzer)
  (:import
   (com.microsoft.playwright BrowserType$LaunchOptions Playwright)))

;; ## Playwright or Etaoin?
;;
;; [Playwright] and [Etaoin] both provide an API for controlling web browser behavior.
;; Playwright provides packages for use in TypeScript/JavaScript, Python, .NET and Java.
;; Etaoin is a pure Clojure implementation.
;;
;; In this article, we use Playwright through the Playwright Java API.
;;
;; [Playwright]: https://playwright.dev/
;; [Etaoin]: https://github.com/clj-commons/etaoin

^{:nextjournal.clerk/visibility {:code :hide}}
(let [yes "yes ✅"
      no "no ❌"]
  (clerk/caption "Comparison: Playwright with java interop and Etaoin"
   (clerk/table
    (clerk/use-headers
     [["Criterion"        "Playwright w/ Java interop"    "Etaoin"]
      ["Use from Clojure" "Clojure java interop, objects" "Idiomatic Clojure interface"]
      ["Use from Typescript" yes no]
      ["Use from Python"     yes no]
      ["Use from .NET"       yes no]
      ["Browser installation"
       "automatic, with the maven package"
       "user installs browser web drivers manually"]]))))

;; Pick what suits your needs, or try both.

;; ## Playwright REPL setup
;;
;; We need a playwright instance and a firefox instance to poke at.
;;
;; `defonce` ensures we get _one_ instance when we start our REPL and Clerk.
;; Restarts are manual in a comment block.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn make-playwright [] (Playwright/create))
^{:nextjournal.clerk/visibility {:result :hide}}
(defonce playwright (make-playwright))

^{:nextjournal.clerk/visibility {:result :hide}}
(defn make-firefox [pw]
  (-> pw
      (.firefox)
      (.launch
       (-> (BrowserType$LaunchOptions.)
           (.setHeadless false)))))

^{:nextjournal.clerk/visibility {:result :hide}}
(defonce firefox (make-firefox playwright))

^{:nextjournal.clerk/visibility {:result :hide}}
(comment
  (.close playwright)
  (alter-var-root #'playwright (constantly (make-playwright)))
  (.close firefox)
  (alter-var-root #'firefox (constantly (make-firefox playwright)))

  :rcf)

;; ## Screenshots
;;
;; We're going to generate screenshots for a bunch of pages, and look at them.
;; We'll store the images on disk, with a file name we can compute from the URL.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn url->png-filename [url]
  (str (nextjournal.clerk.analyzer/valuehash url) ".png"))

^{:nextjournal.clerk/visibility {:result :hide}}
(def urls
  (for [url ["https://mikrobloggeriet.no"
             "https://play.teod.eu"
             "https://teod.eu"
             "https://play.teod.eu/aphorisms"]]
    {:url url
     :filename (url->png-filename url)}))

^{:nextjournal.clerk/visibility {:result :hide}}
(defn short-filename-str [s]
  (let [l (.length s)]
    (str
     (subs s 0 5)
     "[...]"
     (subs s (- l 8) l))))

(let [urls-pretty
      (->> urls
           (map (fn [m]
                  (-> m
                      (update :filename short-filename-str)
                      (update :url #(clerk/html [:a {:href %} %]))))))]
  (clerk/caption "We hash URLs to get valid file names"
                 (clerk/table urls-pretty)))

(comment
  (doseq [url urls]
    (let [page (.newPage firefox)]
      (.navigate page url))
    )

  )
