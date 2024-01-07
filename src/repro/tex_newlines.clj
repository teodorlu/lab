(ns repro.tex-newlines
  (:require
   [nextjournal.clerk :as clerk]))

;; Is there an endorsed way to use TeX to format math in Clerk without forcing newlines for each math expression?

;; I'm working with maps where I'd like to view the values with TeX.
;; I've been using `nextjournal.clerk/tex` like this:

{:a1 (clerk/tex "5.38 \\times 10^3 \\operatorname{mm}^{2}")
 :a2 (clerk/tex "6.26 \\times 10^3 \\operatorname{mm}^{2}")}

;; Ideally, I'd like the the `clerk/tex` result to behave like Clojure maps, vectors, numbers, strings and keywords.
;; The user can choose to show the value on its own line or on many lines through the object browser UI.
