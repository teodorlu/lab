;; # `pandoc-edn`: an intermediate representation for rich text

(ns pandoc-edn
  (:require [teodorlu.pandoc.alpha2 :as pandoc]
            [babashka.process]
            [clojure.java.shell]))

;; Go to the latest version of this notebook:
;;
;;   https://github.clerk.garden/teodorlu/clerk-stuff?update=1
;;
;; -----
;;
;; Endgame: provide a nice, idiomatic Clojure interface to pandoc.
;;
;; Status: ended up spending a lot of time thinking about how to interface with
;; pandoc. I shell out to Pandoc. But that pandoc dependency is implicit. So
;; what do I do?
;;
;; At least I can ensure loose coupling on pandoc.
;;
;; `pandoc-edn` has an atom that controls how it shells out to pandoc:

pandoc/!exec-fn

;; The library user has to set this themselves. Two libraries for shelling out
;; to external processes are `clojure.java.shell` and `babashka.process`. Let's
;; try both.

;;
;; with `babashka.process`:

(defn pandoc-exec-fn-bb-process
  "Shell out to pandoc with babashka.process"
  [s args]
  (slurp (:out (apply babashka.process/process {:in s}
                      "pandoc" args))))

(comment
  (pandoc-exec-fn-bb-process "Good morning!" ["--from" "markdown" "--to" "html"]))
;; comment out to make this namespace compile on clerk.garden

;; with `clojure.java.shell`:

(defn pandoc-exec-fn-clojure-java-shell
  "SHell out to pandoc with clojure.java.shell"
  [s args]
  (let [cmd (concat ["pandoc"] args [:in s])]
    (:out (apply clojure.java.shell/sh cmd))))

(comment
  (pandoc-exec-fn-clojure-java-shell "Good morning!" ["--from" "markdown" "--to" "html"]))
;; comment out to make this namespace compile on clerk.garden

;; with both libraries side-by-side, I prefer `babashka.process`. No funny
;; `concat`, no function keyword arguments. Option map, then args. Simple.

(reset! pandoc/!exec-fn pandoc-exec-fn-bb-process)

;; ... aaand this doesn't work on the clerk runner.
;; becuase it the clerk runner doesn't provide a pandoc binary.
;;
;; Apparently, both Nextjournal folks and borkdude are interested in a pandoc
;; pod for clojure. More details on Clojurians Slack:
;;
;;   https://clojurians.slack.com/archives/C035GRLJEP8/p166619657350352
;;
;; Pandoc source:
;;
;;   https://github.com/jgm/pandoc
;;
;; Rotokim/stash is a Haskell tool with native babashka pod support:
;;
;;   https://github.com/rorokimdim/stash/tree/f07f90316531cb0b3eafaa481ab72b8ca59525f6/src/BabashkaPod.hs
;;
;; Rough draft to get this working:
;;
;;  1. Rip out hair getting an up-to-date Haskell environment working
;;  2. Slash out parts of BabashkaPod.hs from stash to try to get something working
;;  3. Provide bencode / POD API
;;  4. Rip out remaining hair trying to figure out how to get multi-target Haskell compilation working
;;
;;      (or steal more from rorokimdim, see
;;      https://github.com/rorokimdim/stash/tree/f07f90316531cb0b3eafaa481ab72b8ca59525f6/.github/workflows/release.yml
;;      )
;;
;; I wonder how much of a bribe https://github.com/rorokimdim (Amit Shrestha)
;; would need to be willing to help with this?
