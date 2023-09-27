#!/usr/bin/env bb
(ns babashka-cli-intro.w1-git-pull-folder)

(get [] 1)

(defn -main [& args])

(when (= *file* (System/getProperty "babashka.file"))
  ;; the current file is being executed as a script with babashka
  ;; Run main!
  (apply -main *command-line-args*))

;; this approach makes a lot of things "just work".
;;
;; 1. We can use this file as a library from other files on babashka and on the JVM
;; 2. We can install a CLI by pointing from somewhere to this main function
;; 3. We can use this file as a standalone CLI
;;      either with bb:
;;
;;          bb src/babashka_cli_intro/w1_git_pull_folder
;;
;;      or with the script itself, presuming it has been set as executable:
;;
;;          chmod +x  src/babashka_cli_intro/w1_git_pull_folder
;;          ./src/babashka_cli_intro/w1_git_pull_folder
;;
;; In short, we can get normal Clojure goodies and normal shell scripting
;; goodies. And we can choose to develop on the JVM or on babashka. I like using
;; the JVM because my dev tooling (CIDER, Emacs) works well with JVM Clojure.
;; Note: Babashka also supports nREPL, and CIDER has built-in support for
;; jacking in with a babashka runtime.
