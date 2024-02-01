(ns do.preload-fonts
  (:require
   [clojure.string :as str]))

(def links-src (str/trim "
et-book/et-book-roman-line-figures/et-book-roman-line-figures.eot
et-book/et-book-roman-line-figures/et-book-roman-line-figures.ttf
et-book/et-book-roman-line-figures/et-book-roman-line-figures.woff
et-book/et-book-roman-line-figures/et-book-roman-line-figures.svg
et-book/et-book-bold-line-figures/et-book-bold-line-figures.eot
et-book/et-book-bold-line-figures/et-book-bold-line-figures.ttf
et-book/et-book-bold-line-figures/et-book-bold-line-figures.woff
et-book/et-book-bold-line-figures/et-book-bold-line-figures.svg
et-book/et-book-semi-bold-old-style-figures/et-book-semi-bold-old-style-figures.svg
et-book/et-book-semi-bold-old-style-figures/et-book-semi-bold-old-style-figures.ttf
et-book/et-book-semi-bold-old-style-figures/et-book-semi-bold-old-style-figures.woff
et-book/et-book-semi-bold-old-style-figures/et-book-semi-bold-old-style-figures.eot
et-book/et-book-display-italic-old-style-figures/et-book-display-italic-old-style-figures.ttf
et-book/et-book-display-italic-old-style-figures/et-book-display-italic-old-style-figures.woff
et-book/et-book-display-italic-old-style-figures/et-book-display-italic-old-style-figures.eot
et-book/et-book-display-italic-old-style-figures/et-book-display-italic-old-style-figures.svg
et-book/et-book-roman-old-style-figures/et-book-roman-old-style-figures.eot
et-book/et-book-roman-old-style-figures/et-book-roman-old-style-figures.ttf
et-book/et-book-roman-old-style-figures/et-book-roman-old-style-figures.svg
et-book/et-book-roman-old-style-figures/et-book-roman-old-style-figures.woff
"))

(defn link->html-font-preload [link]
  (str "<link rel=\"preload\" href=\"" link "\" as=\"font\" crossorigin>"))

(comment

  (doseq [l (str/split-lines links-src)]
    (println (link->html-font-preload l)))


  )
