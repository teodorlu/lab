(ns pandoc-edn
  (:require [teodorlu.pandoc.alpha2 :as pandoc]
            [babashka.process]
            [clojure.java.shell]))

;; # `pandoc-edn`: an intermediate representation for rich text
;;
;; Endgame: provide a nice, idiomatic Clojure interface to pandoc.
;;
;; Status: ended up spending a lot of time thinking about how to interface with
;; pandoc. I shell out to Pandoc. But that pandoc dependency is implicit. So
;; what do I do?
;;
;; At least I can ensure loose coupling on pandoc.
;;
;; =pandoc-edn= has an atom that controls how it shells out to pandoc:

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

(pandoc-exec-fn-bb-process "Good morning!" ["--from" "markdown" "--to" "html"])

;; with `clojure.java.shell`:

(defn pandoc-exec-fn-clojure-java-shell
  "SHell out to pandoc with clojure.java.shell"
  [s args]
  (let [cmd (concat ["pandoc"] args [:in s])]
    (:out (apply clojure.java.shell/sh cmd))))

(pandoc-exec-fn-clojure-java-shell "Good morning!" ["--from" "markdown" "--to" "html"])

;; with both libraries side-by-side, I prefer `babashka.process`. No funny
;; `concat`, no function keyword arguments. Option map, then args. Simple.

(reset! pandoc/!exec-fn pandoc-exec-fn-bb-process)
