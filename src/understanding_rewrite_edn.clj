;; # Working with Babashka: shell meets Clojure
;;
;; Making a contribution to `babashka/neil`
;;
;; Working with data, contributing to open source, and why you should care about Babashka and Neil.

^{:nextjournal.clerk/toc true
  :nextjournal.clerk/visibility {:code :hide}}
(ns understanding-rewrite-edn
  (:require [nextjournal.clerk :as clerk]))

;; TODO write intro / what is this?
;; Why should someone read this?
;; Who is it for?
;; What should its name be?
;;
;; Key points:
;;
;; 1. Data modeling.
;;    I want to get good at modeling with plain data!
;; 2. Contributing to open source.
;;    What does it look like?
;;    How could you get started?
;; 3. Babashka and Neil
;;    Why should you try building something with Babashka?
;;    Why should you care about Neil?
;; 4. Babashka: REPL not required.
;;    How just working with a binary can be simpler.
;;

;; In my pursuit to get better at data modeling, I'd like to understand approaches to whitespace-preserving data rewriting.
;;
;; Relevant libraries:
;;
;; 1. [rewrite-clj] - A widely used utility library for rewriting clojure code and EDN data
;; 2. [rewrite-edn] - A layer on top of rewrite-clj
;;
;; [rewrite-clj]: https://github.com/clj-commons/rewrite-clj
;; [rewrite-edn]: https://github.com/borkdude/rewrite-edn
;;
;; It's not 100 % clear to me how these two differ.
;;
;; ## Backdrop: an inconvenience in Neil
;;
;; [neil] is a command line tool for working with Clojure projects.
;; I use Neil to add and update dependencies.
;; I enjoy building my own tools, and Neil has given me a chance to do just that.
;; It's a small Clojure/babashka codebase that you can read all of.
;; And it's just a CLI.
;; So no database, no auth, just a tool that you can use locally.
;; Thanks to Babashka and babashka/bbin, working on it locally is also a joy.
;; But I won't get into that now!
;;
;; [neil-quickadd]: https://github.com/teodorlu/neil-quickadd
;; [neil]: https://github.com/babashka/neil
;;
;; I recently stumpled over an annoyance in the `deps.edn` created by `neil new mylib`.
;; Do you see anything that stands out?

^{:nextjournal.clerk/visibility {:result :hide}}
(quote  ; bad
 {:paths ["src" "resources"]
  :deps {org.clojure/clojure {:mvn/version "1.11.1"}}
  :aliases
  {:test
   {:extra-paths ["test"]
    :extra-deps {org.clojure/test.check {:mvn/version "1.1.1"}
                 io.github.cognitect-labs/test-runner
                 {:git/tag "v0.5.1" :git/sha "dfb30dd"}}}
   :build {:deps {io.github.clojure/tools.build
                  {:git/tag "v0.9.2" :git/sha "fe6b140"}
                  slipset/deps-deploy {:mvn/version "0.2.0"}}
           :ns-default build} :neil {:project {:name mylib/mylib}}}})

;; Let's try that once more, with the indentation that I'd prefer.

^{:nextjournal.clerk/visibility {:result :hide}}
(quote  ; good
 {:paths ["src" "resources"]
  :deps {org.clojure/clojure {:mvn/version "1.11.1"}}
  :aliases
  {:test
   {:extra-paths ["test"]
    :extra-deps {org.clojure/test.check {:mvn/version "1.1.1"}
                 io.github.cognitect-labs/test-runner
                 {:git/tag "v0.5.1" :git/sha "dfb30dd"}}}
   :build {:deps {io.github.clojure/tools.build
                  {:git/tag "v0.9.2" :git/sha "fe6b140"}
                  slipset/deps-deploy {:mvn/version "0.2.0"}}
           :ns-default build}
   :neil {:project {:name mylib/mylib}}}})  ; 👈 Look here

;; See?
;; That `:neil :project :name` bit didn't get its own line.
;;
;; This is what we'd like to fix.

;; ## Tracing down the offending code
;;
;; Neil is so small and neat that we can often get a better development experience outside of a REPL than inside it.
;;
;; You can work with Neil in a number of different ways:
;;
;; 1. The code runs on Babashka and JVM Clojure
;; 2. There are tests
;; 3. You can REPL into the codebase and evaluate stuff
;; 4. You can install the CLI as a "dead" single file babashka binary
;; 5. You can "live install" a development binary that _instantly_ reflects any changes you make in the Neil source code.
;;
;; I'm going with 5 -- live installed development binary.
;; You'll find the following in the end of the Neil README:
;;
;; > Or install a development version with [bbin][bbin]:
;; >
;; > ```clojure
;; > $ bbin install . --as neil-dev --main-opts '["-m" babashka.neil/-main]'
;; > ```
;; >
;; > [bbin]: https://github.com/babashka/bbin
;;
;; So, where's that offending code?
;; I knew two things:
;;
;; 1. The bad behavior happened when I ran `neil new lib mylib`
;; 2. The bad behavior related to `:neil {:project {:name ,,,}}` metadata.
;;
;; My approach was:
;;
;; > Run reproducible experiments to reproduce real behavior.
;; > Add println's to get closer to _when_ the weird behavior ocurred.
;;
;; My reproducible experiments were run with this shell one liner:
;;
;; ```bash
;; $ rm -rf mylib && neil-dev new lib mylib
;; ```
;;
;; And the printlns I copied around were these:
;;
;; ```clojure
;; (println ";;  0")
;; (println (slurp "/home/teodorlu/tmp/temp-2023-02-21/neil-bug/mylib/deps.edn"))
;; ```
;;
;; Reading from the `neil new` subcommand and searching for `neil project` lead me to the namespace [babashka.neil.project].
;;
;; [babashka.neil.project]: https://github.com/babashka/neil/tree/65a078a24462ab06fc7bc3bd5e240f4adfaa7f8a/src/babashka/neil/project.clj

;; Lets simulate a real run! We're working with Clerk, after all, a tool for literate programming.
;; Note: I'm hard-coding a bunch of paths here.
;; This is a great choice for local debugging, but not something something I'd commit and push.
;; Don't push stuff that breaks on others!

(require '[babashka.process :refer [shell]])

(defn bash [s {:keys [dir]}]
  (let [p (shell {:out :string :dir dir} "bash" "-c" s)]
    (clerk/html [:pre (:out p)])))

;; If we run unpatched `neil` from master, we don't get much output:

(bash "rm -rf mylib && neil new lib mylib"
      {:dir "/home/teodorlu/tmp/temp-2023-02-21/neil-bug"})

;; We make the following changes to our modified `neil-dev` binary:

^{:nextjournal.clerk/visibility {:result :hide}}
(quote
 (defn assoc-project-meta!
   "Updates deps-file's :neil :project `k` with `v`"
   [{:keys [dir deps-file k v]
     :as opts}]
   (println ";;  0")
   (println (slurp "/home/teodorlu/tmp/temp-2023-02-21/neil-bug/mylib/deps.edn"))
   (ensure-neil-project opts)
   (println ";;  1")
   (println (slurp "/home/teodorlu/tmp/temp-2023-02-21/neil-bug/mylib/deps.edn"))
   (let [deps-file (resolve-deps-file dir deps-file)
         deps-edn (slurp deps-file)
         nodes (r/parse-string deps-edn)
         nodes (r/assoc-in nodes [:aliases :neil :project k] v)]
     (spit deps-file (str nodes)))
   (println ";;  2")
   (println (slurp "/home/teodorlu/tmp/temp-2023-02-21/neil-bug/mylib/deps.edn"))))

;; Running the same statement again gives us more verbose output:

(bash "rm -rf mylib && neil-dev new lib mylib"
      {:dir "/home/teodorlu/tmp/temp-2023-02-21/neil-bug"})

;; We can definitively conclude that the weird behavior happens in `assoc-project-meta!`!

;; ## Remaining work
;;
;; 1. Create a reproducible example of what we're actually doing with rewrite-edn in `assoc-project-meta!`
;; 2. Create a reproducible example of using `rewrite-edn` to achieve what we want.
;; 3. Make the required changes in `neil`
;; 4. Test
;; 5. PR!























^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html ; add some trailing whitespace
 (into [:div (repeat 20 [:br])]))
