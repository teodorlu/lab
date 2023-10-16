;; # Plotting lines with Clerk

(ns teodorlu.lab.plot.lines
  (:require
   [nextjournal.clerk :as clerk]))

;; ## loglog plot with vega + lines

(clerk/vl
 {"mark" "point",
  "data" {"values" [{"x" 0, "y" 1}
                    {"x" 1, "y" 10}
                    {"x" 2, "y" 100}
                    {"x" 3, "y" 1000}
                    {"x" 4, "y" 10000}
                    {"x" 5, "y" 100000}
                    {"x" 6, "y" 1000000}
                    {"x" 7, "y" 10000000}]},
  "encoding" {"x" {"field" "x", "type" "quantitative"},
              "y" {"field" "y", "scale" {"type" "log"}}}}
 )
