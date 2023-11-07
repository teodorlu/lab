;; # Steel beams, SI units and Clojure multimethods: a match made in heaven
;;
;; > Flexible software must be more flexible than all known cases.
;;
;; I'm paraphrasing [Software Design for Flexibility] by [Chris Hanson] and [Gerald Jay Sussman] from memory, don't expect to find the exact quote.
;;
;; [Software Design for Flexibility]: https://mitpress.mit.edu/books/software-design-flexibility
;; [Chris Hanson]: https://people.csail.mit.edu/cph/
;; [Gerald Jay Sussman]: http://groups.csail.mit.edu/mac/users/gjs/gjs.html
;;
;; I believe they are right.
;; Flexible systems must be more general than their application.
;; Otherwise the flexible system is not an abstraction, it's just a collection of code.
;;
;; ## Clojure is a flexible language
;;
;; In a discussion with a friend who enjoys programming greatly, I said "I don't really care about syntax".
;; That statement surprised him.
;; Frankly, the statement surprised me too.
;; Syntax isn't irrelevant.
;; But it's not _the goal_.
;; Some programming languages depend greatly on the syntax.
;; Others don't.
;;
;; Clojure establishes syntax for sequential data, associative data, sets, function calls and macro calls.

^{:nextjournal.clerk/toc true}
(ns steel-beams-si-units-clojure-multimethods
  (:refer-clojure :exclude [* / + -])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.set :as set]
   [clojure.string :as str]
   [nextjournal.clerk :as clerk]
   [taoensso.nippy :as nippy]))

^{:nextjournal.clerk/visibility {:code :hide
                                 :result :hide}}
(comment
  ;; pitch
  ;;
  ;; In which you will learn
  ;;
  ;; - whether pixels and millimeters are the same thing
  ;; - the difference between a flange and a web
  ;; - how to draw steel beam cross section profiles
  )

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/example
 ;; syntax for sequential data:
 [1 2 3]

 ;; syntax for associve data:
 {:name "Teodor"
  :thesis {:title "Finite element implementation of lower-order strain gradient plasticity in Abaqus"
           :url "https://www.teodorheggelund.com/static/heggelund15.pdf"}}

 ;; syntax for sets:
 #{:typed-fp :untyped-fp :cqrs}

 ;; syntax for function calls:
 (last [1 2 3])
 (set/union #{:typed-fp :untyped-fp :cqrs}
            #{:spaghetti :shekshuka})

 ;; syntax for macro calls:
 (let [topics (set/union #{:typed-fp :untyped-fp :cqrs}
                        #{:spaghetti :shekshuka})]
   (str "consider exploring " (name (rand-nth (vec topics))) " today!")))

;; If you accept these five decisions about syntax, you can mostly do whatever you want afterwards.

;; You can define your own functions, and your own macros.
;; `last` is defined as `clojure.core/last`, `set/union` is defined in `clojure.set/union.`
;; Let is a macro defined in `clojure.core/let`.
;; The only reason you can write `let` and have that automatically refer to `clojure.core/let` is `clojure.core` is required by default.
;; That default can be disabled.

;; Notice all the things that are missing?
;;
;; - Syntax for function definition — `fn` is a plain macro.
;; - Syntax for type definitions — you can choose a library for checking data
;;   (like [clojure spec] or [malli], _if_ you want.
;; - Syntax for for loops — `for` is a plain macro
;; - Syntax for while loops — `loop` and `reduce` are plain macros.
;;   You can use those, or write your own alternatives.
;; - Syntax for defining types — `deftype`, `defrecord` and `defprotocol` are macros.
;;
;; [clojure spec]: https://clojure.org/guides/spec
;; [malli]: https://github.com/metosin/malli
;;
;; This is good!
;;
;; Take the time to learn and understand vectors (sequential data), maps (associative data), sets (unique elements only) and functions.
;; Idiomatic Clojure code is functions transforming data.
;; Leave the rest for when you actually need it!

;; ## Steel beams
;;
;; > Wait, what about the steel beams?
;; > I thought there was supposed to be steel beams.
;;
;; Yes!
;; Steel beams coming right up.
;;
;; Flexible languages can solve a wide variety of problems.
;; A problem you might pick for yourself is to design steel beams.
;; Let's see if Clojure is a good fit.

;; This is a _steel beam_:

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/caption "Steel beam supporting the first floor of a house"
               (clerk/image (io/resource "steel_beams/I-Beam_002.jpg")))

;; Image [from Wikipedia][I-Beam_002.jpg-source], retreived 2023-10-28, licensed CC BY-SA 3.0.
;;
;; [I-Beam_002.jpg-source]: https://commons.wikimedia.org/wiki/File:I-Beam_002.JPG

;; This a figure of steel beam's _cross section_:

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/caption "Cross section of an I-shape beam"
               (clerk/html (slurp (io/resource "steel_beams/I-BeamCrossSection-NORWEGIAN.svg"))))

;; Image [from Wikipedia][I-BeamCrossSection.svg-source], retreived 2023-10-28, licensed CC BY-SA 3.0.
;; SVG labels have been altered to match the names used in this article.
;;
;; [I-BeamCrossSection.svg-source]: https://commons.wikimedia.org/wiki/File:I-BeamCrossSection.svg

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(comment
  (clerk/clear-cache!))


;; Is copy-pasting stuff from Wikipedia the end game of content creation?
;; No!
;; Let's make our own figures.
;;
;; We will be working on beams as maps.

^{:nextjournal.clerk/visibility {:code :hide}}
(let [labels [{:norwegian :r       :pretty "r"                  :description "Curve radius between flanges and beams"}
              {:norwegian :wy      :pretty "W_y"                :description "Strong axis bending moment resistance"}
              {:norwegian :s       :pretty "s"                  :description "Web thickness"}
              {:norwegian :prefix  :pretty "\\textit{prefix}"   :description "Beam profile name prefix, eg IPE or HEA"}
              {:norwegian :wz      :pretty "W_z"                :description "Weak axis bending moment resistance"}
              {:norwegian :h       :pretty "h"                  :description "Beam cross section height"}
              {:norwegian :b       :pretty "b"                  :description "Beam cross section width"}
              {:norwegian :iz      :pretty "I_z"                :description "Weak axes bending stiffness"}
              {:norwegian :t       :pretty "t"                  :description "Flange thickness"}
              {:norwegian :iy      :pretty "I_y"                :description "Strong axis bending stiffness"}
              {:norwegian :profile :pretty "\\textit{profile}"  :description "Profile number, eg 300 for IPE300"}
              {:norwegian :a       :pretty "A"                  :description "Cross section area"}

              ]]
  (clerk/table (for [label (sort-by :norwegian labels)]
                 (-> label
                     (update :pretty clerk/tex)
                     (set/rename-keys {:description "Description"
                                       :norwegian "Label"
                                       :pretty "Notation for people who like math"})))))

;; An IPE300 beam is represented as this:

^{:nextjournal.clerk/visibility {:code :hide}}
{:r 15, :wy 557, :s 7.1, :prefix "IPE", :wz 80.5, :h 300, :b 150, :iz 6.04, :t 10.7, :iy 83.6, :profile 300, :a 5.38}

;; Let's draw such beams with SVG:

(defn i-shape-steel-beam->svg [beam]
  (let [plus clojure.core/+
        minus clojure.core/-
        div clojure.core//
        mult clojure.core/*

        margin 4.5
        margin*2 (mult margin 2)

        ;; Profile parameters
        {:keys [h b s t r]} beam
        ;;
        ;; Notation for paths with SVG
        ;; See
        ;;
        ;;     https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial/Paths
        ;;
        ;; to learn how to work with SVG pathsSVG paths
        M "M"
        l "l"
        a "a"

        -r (minus r)
        r*2 (mult r 2)
        flange-tip-length (div (minus b s) 2)
        web-inner-height (minus h (mult 2 t))
        path [M margin margin
              l b 0
              l 0 t
              l (minus (minus flange-tip-length r)) 0 ; top right corner

              ;; MDN explains how to draw curve segments, _arcs_:
              ;;
              ;; https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial/Paths#arcs
              ;;
              ;; a rx ry x-axis-rotation large-arc-flag sweep-flag dx dy              "a" r r 0
              a  r  r  0               0              0          -r r
              l 0 (minus web-inner-height r*2)  ; steg høyre
              a r r 0 0 0 r r

              l (minus flange-tip-length r) 0
              l 0 t
              l (minus b) 0
              l 0 (minus t)

              l (minus flange-tip-length r) 0
              a r r 0 0 0 r (minus r)
              l 0 (minus (minus web-inner-height r*2)) ;; web left
              a r r 0 0 0 (minus r) (minus r)
              l (minus (minus flange-tip-length r)) 0
              "Z"
              ]]
    [:svg {:width (plus margin*2 (:b beam))
           :height (plus margin*2 (:h beam))}
     [:path {:d (str/join " " path)
             :fill "transparent"
             :stroke "black"}]]))

(let [ipe300 {:r 15, :wy 557, :s 7.1, :prefix "IPE", :wz 80.5, :h 300, :b 150, :iz 6.04, :t 10.7, :iy 83.6, :profile 300, :a 5.38}]
  (clerk/caption "My SVG of a steel beam!"
                 (clerk/html (i-shape-steel-beam->svg ipe300))))

;; Amazing!
;; It works!
;; Clojure is great!
;; Data is great!
;; SVG is great!
;; Clerk is great!

;; Now, what is _wrong_ with the function above?
;; Don't scroll.
;; No, stop.
;; Think.
;;
;; We've complected two things.
;; Give yourself a solid 20 seconds staring out a window to figure out _which two things_ are currently complected.

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])

;; Active reading seriously helps, I promise!

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])

;; We've complected pixels and millimeters.
;; Why is the figure 300 pixels high?
;; I have no idea!
;;
;; Or, I know why, but it's a very bad reason.
;; It's because we've assumed that millimeters and pixels are the same thing.

^{:nextjournal.clerk/visibility {:code :hide}}
(let [padding-top-bottom "1em"]
  (clerk/html
   [:div.flex.justify-center
    {:style {:padding-top padding-top-bottom :padding-bottom padding-top-bottom}}
    [:strong [:em "Millimeters and pixels are not the same thing."]]]))

;; Let's fix that.

;; ## Numbers with unit
;;
;; Our solution is to invent a number type that respects units.
;; We will name our "number with unit" type "with-unit".

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(comment
  ;; impl based on mikera's question here:
  ;;
  ;;     https://stackoverflow.com/questions/3018372/overriding-equals-hashcode-and-tostring-in-a-clojure-deftype
  )

(deftype WithUnit [number unit]
  Object
  (hashCode [_] (bit-xor (hash number) (hash unit)))
  (equals [self other]
    (and (instance? WithUnit other)
         (= (.number self) (.number other))
         (= (.unit self) (.unit other)))))


^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(do
  ;; https://github.com/taoensso/nippy/wiki/1-Getting-started#custom-types
  ;;
  ;; Support Clerk by supporting Nippy.
  ;; Hide it from the text, this isn't the point.
  (nippy/extend-freeze WithUnit :teodorlu.lab.steel-beams-si-units-clojure-multimethods/WithUnit
                       [x data-output]
                       (.writeUTF data-output (pr-str (.number x)))
                       (.writeUTF data-output (pr-str (.unit x))))

  (nippy/extend-thaw :teodorlu.lab.steel-beams-si-units-clojure-multimethods/WithUnit
                     [data-input]
                     (let [x (edn/read-string (.readUTF data-input))
                           unit (edn/read-string (.readUTF data-input))]
                       (WithUnit. x unit))))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(comment
  (nippy/thaw (nippy/freeze (WithUnit. 5 {:si/m 2})))

  (-> (WithUnit. 5 {:si/m 2})
      nippy/freeze
      nippy/thaw)

  ;; => #object[steel_beams_si_units_clojure_multimethods.WithUnit 0x2f7197fb "steel_beams_si_units_clojure_multimethods.WithUnit@7a1491a9"]
  )

;; We represent a unit as a map from a base unit to exponent.

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/caption "Examples of units as maps from base unit to exponent"
               (clerk/table [{:name "meter"                    :value {:si/m 1}}
                             {:name "square meter"             :value {:si/m 2}}
                             {:name "second"                   :value {:si/s 1}}
                             {:name "per second"               :value {:si/s -1}}
                             {:name "meters per square second" :value {:si/m 1 :si/s -2}}
                             ]))

;; Let's not invent types when we don't have to.
;;
;; We can now represent 300 mm and preserve the unit:

(str (WithUnit. (clojure.core// 300 1000) {:si/m 1}))

;; Perhaps we can persuade Clerk to show our units in a more appealing way?
;; We start by looking at Clerk's provided viewers.

clerk/default-viewers

;; A viewer is a map.
;; What kind of keys are there?

(->> clerk/default-viewers
     (mapcat keys)
     (into #{})
     sort)

;; For our SI Unit viewer, I believe we need:
;;
;; - `:name` — a viewer name
;; - `:pred` — a predicate that tells Clerk _when_ to use the viewer
;; - A way to actually implement our viewer, perhaps `:render-fn` or `:transform-fn`.
;;
;; The Clerk Docs provide an example of a [simple viewer using :transform].
;; From the example, we make our own little viewer:
;;
;; [simple viewer using :transform]: https://github.clerk.garden/nextjournal/book-of-clerk/commit/b4c03cfb272f516a287c51133dd2dc0a71f274f0/#transform

(clerk/with-viewer {:transform-fn (clerk/update-val (constantly "LOL IT'S A JOKE"))}
  "A very serious sentence")

;; Nice!
;; New viewer in one line of code.
;;
;; Can it show Math?

(clerk/with-viewer {:transform-fn (clerk/update-val
                                   (constantly (clerk/tex "a^2 + b^2 = c^2")))}
  "A very serious sentence that is totally ignored in favor of math.")

;; It's math!

;; But:
;;
;; 1. We don't want to set the viewer manually on each expression we write.
;; 2. We also don't want to break all the existing viewers.
;;
;; I'm thinking we want to write a viewer that applies _only to our new unit type_.

(defn with-unit? [x] (instance? WithUnit x))

(clerk/example
 (with-unit? (WithUnit. (clojure.core// 300 1000) {:si/m 1}))
 (with-unit? 3)
 (with-unit? "iiiiiiiiiiiiiiiiiiiiii"))

;; Looks about right.
;;
;; Now, let's render m^2/s.

(defn unit->tex
  "Convert from a unit as data to unit as TeX.

  Unit-as-map is from base unit to exponent:

    {:m 2 :s -1}

  In TeX, we render m^2 above the line, and s below:

    \"\\frac{\\operatorname{m}^{2}}{\\operatorname{s}}\""
  [unit]
  (when (map? unit)
    (let [numerator (filter (comp pos? val) unit)
          numerator (if (seq numerator)
                      numerator
                      "1")
          denominator (->> unit
                           (filter (comp neg? val))
                           (map (fn [[baseunit exponent]]
                                  [baseunit (clojure.core/- exponent)])))
          base+exp->tex
          (fn [[base exp]]
            (str "\\operatorname{" (name base) "}"
                 (when (not= 1 exp)
                   (str "^{" exp "}"))))]
      (if-not (seq denominator)
        (str/join " " (map base+exp->tex numerator))
        (str "\\frac{" (str/join " " (map base+exp->tex numerator)) "}"
             "{" (str/join " " (map base+exp->tex denominator)) "}")))))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/example
 (unit->tex {:m 2 :s -1})
 (clerk/tex (unit->tex {:m 2 :s -1})))

;; That looks like what I had in mind.
;; We also want _numbers with SI units_.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn with-unit->tex [with-unit]
  (str (cond-> (.number with-unit)
         ratio? double)
       " "
       (unit->tex (.unit with-unit))))

;; For with-units (what a weird noun), the raw TeX and the rendered TeX look different:

(with-unit->tex (WithUnit. (clojure.core// 300 1000) {:si/m 1}))

(clerk/tex (with-unit->tex (WithUnit. (clojure.core// 300 1000) {:si/m 1})))

;; Finally, we can create a viewer.

^{:nextjournal.clerk/visibility {:result :hide}}
(def with-unit-viewer
  {:name `with-unit-viewer
   :pred with-unit?
   :transform-fn (clerk/update-val (fn [unit]
                                     (clerk/tex (with-unit->tex unit))))})

^{:nextjournal.clerk/visibility {:result :hide}}
(clerk/add-viewers! [with-unit-viewer])

(do
  (WithUnit. (clojure.core// 300 1000) {:si/m 1}))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(comment
  ;; Looks like i STILL dont't have a working Nippy solution.
  ;;
  ;; When I drop the `do` above, Clerk chokes.
  )

;; It's working!
;; Time to implement *.
;; We're going to use multimethods to support plain numbers numbers without units.

;; First, we need a dispatch fn for two-arg type-based multimethods.
;; Note that multimethod dispatch functions _take one arg_: the dispatch vector.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn ^:private both-types [a b]
  [(type a) (type b)])

(clerk/example
 (both-types 1 1)
 (both-types 1 (WithUnit. (clojure.core// 300 1000) {:si/m 1})))

;; Note: since we're using `defrecord` to implement our SI units, we will inherit Clojure's value-based equality.
;; That's not what we want!
;; Here's an example:

(=
 (WithUnit. (clojure.core// 300 1000) {:si/m 1})
 (WithUnit. (clojure.core// 300 1000) {:si/m 1 :si/s 0}))

;; Our problem is zero exponents in the exponent map.
;; We can fix this with a contructor that conforms units to the representation we want.

(defn ^:private simplify-unit [x]
  (into {} (remove (fn [[_ exponent]] (= exponent 0)) x)))

^{:nextjournal.clerk/visibility {:result :hide}}
(do
  (defmulti simplify type)
  (defmethod simplify Number [n] n)
  (defmethod simplify WithUnit [x]
    (let [simplified-unit (simplify-unit (.unit x))]
      (if (= {} simplified-unit)
        (.number x)
        (WithUnit. (.number x)
                   simplified-unit)))))

;; This implementation simplifies unitless numbers to plain numbers:

(simplify (WithUnit. (clojure.core// 300 1000) {:si/m 0 :si/s 0}))

;; Then we implement a constructor in terms of the simplifier.

(defn with-unit [number unit]
  (simplify (WithUnit. number unit)))

;; From now on, we will always use this constructor.
;;
;; This text is a single Clojure file.
;; In a library for SI units, would /not/ put the deftype in the public interface.

(do
  (defmulti multiply both-types)

  (defmethod multiply [Number Number]
    [a b]
    (clojure.core/* a b))

  (defmethod multiply [Number WithUnit]
    [a b]
    (with-unit
      (clojure.core/* a (.number b))
      (.unit b)))

  (defmethod multiply [WithUnit Number]
    [a b]
    (with-unit
      (clojure.core/* (.number a) b)
      (.unit a)))

  (defmethod multiply [WithUnit WithUnit]
    [a b]
    (with-unit
      (clojure.core/* (.number a) (.number b))
      (merge-with clojure.core/+
                  (.unit a)
                  (.unit b)))))

;; Finally, we can multiply numbers!

(multiply 100 (with-unit (clojure.core// 300 1000) {:si/m 1}))

(defn *
  ([a] a)
  ([a b] (multiply a b))
  ([a b & args] (reduce multiply (multiply a b) args)))

(multiply 100 (WithUnit. (clojure.core// 300 1000) {:si/m 1}))

(let [height (with-unit (clojure.core// 300 1000) {:si/m 1})]
  (clerk/example
   (* height 0.5)
   (* height height)))

;; ## Thank you
;;
;; To Sam Ritchie, Martin Kavalar and Jack Rusher for being generally awesome, and patient with people who don't yet know the secrets of Lisp.
;; To Gerald Jay Sussman for improving the way we think about programming, and expanding the range of problems we can solve with programming.
;; To Eugene Pakhomov, Joshua Suskalo and Ethan McCue for helping me understand how Java types work with Clojure multimethod type hierarchies.

;; ## Further reading
;;
;; Want to dig deeper?
;;
;; - To learn more about designing flexible software, read [Software Design for Flexibility].
;;   It's good!
;;
;; - To see how a real-world flexible system is built, explore [Emmy]'s architecture.
;;   Emmy is inspired by Gerald Jay Sussman's [scmutils], and I'm strongly guessing experience building scmutils informed the writing of _Software Design for Flexibility_.
;;
;; [Software Design for Flexibility]: https://mitpress.mit.edu/books/software-design-flexibility
;; [Emmy]: https://github.com/mentat-collective/emmy/
;; [scmutils]: https://github.com/Tipoca/scmutils/

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
