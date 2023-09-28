(ns babashka-cli-intro.bb-how)

;; ## hvordan jobber jeg med babashka-kode?
;;
;; 1. Akkurat som Clojure-kode, i en REPL.
;; 2. Pluss at jeg noen ganger har et lokalt installert CLI (feks `neil-dev`)

;; ## hvordan strukturerer jeg babashka-prosjekter?
;;
;; 1. Akkurat som Clojure-prosjekter, med deps.edn
;; 2. Pluss at jeg har en `bb.edn` som peker til `deps.edn` og sier "hent ting derfra".

;; ## caveats
;;
;; 1. Jeg har ikke jobbet i svære Clojure-prosjekter
;; 2. Jeg har ikke jobbet med svære Clojure-kodebaser.
;;
;; Mine erfaringer er fra min bruk, det er lurt å tenke sjæl.

;; ## la oss se hvordan dette funker i praksis.
;;
;; 1. Jeg har laget noen eksempler
;; 2. Prøvd å lage en bash-variant og en Clojure-variant, for å sammenlikne.
