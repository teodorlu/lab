;; # Writing CLI applications with Babashka: an introduction

^{:nextjournal.clerk/toc true
  :nextjournal.clerk/visibility {:code :hide}}
(ns babashka-cli-intro
  (:require
   [babashka.fs :as fs]
   [babashka.process :as process]
   [babashka.cli :as cli]))

;; With babashka, you can write shell scripts as single files with no extra fuzz.
;; Put
;;
;;     (println "Hello, world!")
;;
;; in `hello.clj`, run it with `bb hello.clj`, and you'll see
;;
;;    Hello, world!
;;
;; printed to your terminal.
;;
;; In this guide, we'll go deeper than one-script-one-file.
;; We'll showcase a way to write CLI applications with Clojure that:
;;
;; 1. Are organized like normal Clojure JVM projects
;; 2. That can run both on JVM Clojure and on Babashka
;; 3. That can be interacted with using a Clojure JVM REPL and a Babashka REPL.
;;
;; In other words, you can write CLI applications that start fast, and keep all the nicities from your existing Clojure workflow.
;; Does that sound appealing?
;; Let's dig in.

;; ## A minimal JVM/Babashka CLI
;;
;; To start, you need three files. In `deps.edn`, put:
;;
;;     {}
;;
;; Yeah, it's empty!
;; Now, let's tell Babashka to use the same dependencies.
;; In `bb.edn`, put:
;;
;;     {:deps {SOME_PREFIX.YOURNAME/YOURPROJECT {:local/root "."}}}
;;
;; `SOME_PREFIX.YOURNAME/YOURPROJECT` should be as unique as normal Clojars/Maven dependency coordinates.
;; If you have your own domain, consider using that.
;; Borkdude (the guy who made Babashka) uses `org.babashka/neil` [for neil][neil-bb-edn-coordinates], one of his projects.
;; If your project is on github, consider `io.github.ORGANIZATION/project`.
;; The important aspect is that it needs to be unique.
;;
;; [neil-bb-edn-coordinates]: https://github.com/babashka/neil/blob/6a0f6265759e4529f9062dbeadf20cb2ddf6072f/bb.edn#L3


^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
[]
(do
  ;; helpers for running commands
  ;; and nice to do some coding and not just write text.
  ;; we like programming, after all.

  (defn tempdir-deleted-on-exit [])
  )

(comment
  ;; Jukeslapp
  (clojure.repl/doc fs/list-dir)

  (clojure.repl/doc fs/temp-dir)
  (->> (fs/list-dir (fs/temp-dir))
       (map str)
       sort
       (take 5))

  )

(fs/list-dir
 (fs/temp-dir))

(fs/temp-dir)

;; (fs/with-temp-dir)

(str
 (fs/create-temp-dir))
