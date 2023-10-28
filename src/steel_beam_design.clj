^{:nextjournal.clerk/toc true
  :nextjournal.clerk/visibility {:code :hide}}
(ns steel-beam-design
  (:require
   [clojure.java.io :as io]
   [nextjournal.clerk :as clerk]))

;; # Steel beam design with Clojure
;;
;; You're probably wondering why steel beams look like they do, and want an explanation for that in a nice, functional programming language.
;; You're in luck—that's what you'll be getting today!
;;
;; First, let us establish our vocabulary.
;;
;; This is a _steel beam_:

(clerk/caption "Steel beam supporting the first floor of a house"
 (clerk/image (io/resource "steel_beam_design/I-Beam_002.jpg")))

;; Image [from Wikiepdia][I-Beam_002.jpg-source], retreived 2023-10-28, licensed CC BY-SA 3.0.
;;
;; [I-Beam_002.jpg-source]: https://commons.wikimedia.org/wiki/File:I-Beam_002.JPG

;; This a figure of steel beam's _cross section_:

(clerk/caption
 "Cross section of an I-shape beam"
 (clerk/html (slurp (io/resource "steel_beam_design/I-BeamCrossSection.svg"))))

;; Image [from Wikiepdia][I-BeamCrossSection.svg-source], retreived 2023-10-28, licensed CC BY-SA 3.0.
;;
;; [I-BeamCrossSection.svg-source]: https://commons.wikimedia.org/wiki/File:I-BeamCrossSection.svg
;;
;; This steel beam is an _I-shape beam_.
;; I-shape beams have _flanges_ on the top and on the bottom, and a _web_ in the middle.
;; The flanges and the web are labelled on the figure above.
