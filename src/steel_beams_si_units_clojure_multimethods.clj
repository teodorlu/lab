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
   [clojure.java.io :as io]
   [clojure.math :as math]
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
;; `last` is `clojure.core/last`, `set/union` is `clojure.set/union.`
;; Let is `clojure.core/let`, a macro.
;; The only reason you can write `let` and have that automatically refer to `clojure.core/let` is that all vars from `clojure.core` are required by default (which can be disabled).

;; What about syntax for function definitions?
;; Syntax for type definitions?
;; Syntax for loop constructs?
;; Where are they?
;;
;; - There is no syntax for function definition — `fn` is a macro.
;; - There is no syntax for type definitions — you can choose a library for checking data
;;   (like [clojure spec] or [malli], _if_ you want.
;;   If you do need types, there's `deftype`, `defrecord` and `defprotocol`, all three are macros.
;; - There is no syntax for for loops — `for` is a plain macro
;; - There is no syntax for while loops — `loop` and `reduce` are macros.
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
;; Steel beams coming right up!
;;
;; Flexible languages can solve a wide variety of problems.
;; A problem you might pick for yourself is to design steel beams.
;; Let's see if Clojure is a good fit.

;; This is a steel beam:

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
;;
;; [I-BeamCrossSection.svg-source]: https://commons.wikimedia.org/wiki/File:I-BeamCrossSection.svg

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(comment
  (clerk/clear-cache!))


;; Let's make a similar figure ourselves.
;; We will represent beams as maps.

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
                                       :pretty "Math notation"})))))

;; This map is an IPE300 beam:

^{:nextjournal.clerk/visibility {:code :hide}}
{:r 15, :wy 557, :s 7.1, :prefix "IPE", :wz 80.5, :h 300, :b 150, :iz 6.04, :t 10.7, :iy 83.6, :profile 300, :a 5.38}

;; Let's draw breams with SVG.

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

              ;; a rx ry x-axis-rotation large-arc-flag sweep-flag dx dy              "a" r r 0
              a    r  r  0               0              0          -r r
              l 0 (minus web-inner-height r*2)  ; web, right side
              a r r 0 0 0 r r

              l (minus flange-tip-length r) 0
              l 0 t
              l (minus b) 0
              l 0 (minus t)

              l (minus flange-tip-length r) 0
              a r r 0 0 0 r (minus r)
              l 0 (minus (minus web-inner-height r*2)) ;; web, left side
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
;;
;; To support equality, we implement hashCode and equals.

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(comment
  ;; hashCode and equals implementation is based on mikera's question here:
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
  ;; Ensure WithUnit works with Clerk.
  (alter-var-root #'nippy/*thaw-serializable-allowlist*
                  (fn [list] (conj list "steel-beams-si-units-clojure-multimethods.WithUnit"))))

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
               (clerk/table (->> [{:name "meter"                    :value {:si/m 1}}
                                  {:name "square meter"             :value {:si/m 2}}
                                  {:name "second"                   :value {:si/s 1}}
                                  {:name "per second"               :value {:si/s -1}}
                                  {:name "meters per square second" :value {:si/m 1 :si/s -2}}]
                                 (map #(set/rename-keys % {:name "Unit" :value "Map from base unit to exponent"})))))

;; Let's not invent types when we don't have to.
;;
;; We can now represent 300 mm and preserve the unit:

(str (WithUnit. 0.3 {:si/m 1}))

;; Perhaps we can persuade Clerk to show our units in a more appealing way?
;; We start by looking at Clerk's provided viewers.

clerk/default-viewers

;; A viewer is a map.
;; What keys are used by other viewers?

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
;; We made a toy viewer in one line of code.
;;
;; Can viewers show Math?

(clerk/with-viewer {:transform-fn (clerk/update-val
                                   (constantly (clerk/tex "a^2 + b^2 = c^2")))}
  "A very serious sentence that is totally ignored in favor of math.")

;; It's math! But:
;;
;; 1. We don't want to set the viewer manually on each expression we write.
;; 2. We don't want to break existing viewers.
;;
;; I'm thinking we want to write a viewer that applies _only to our new unit type_.

(defn with-unit? [x] (instance? WithUnit x))

(clerk/example
 (with-unit? (WithUnit. 0.3 {:si/m 1}))
 (with-unit? 3)
 (with-unit? "iiiiiiiiiiiiiiiiiiiiii"))

;; Looks about right. Can we render m^2/s?

(defn unit->tex
  "Convert from a unit as data to unit as TeX.

  Unit-as-map is from base unit to exponent:

    {:m 2 :s -1}

  In TeX, we render m^2 above the line, and s below:

    \"\\frac{\\operatorname{m}^{2}}{\\operatorname{s}}\""
  [unit]
  (when (map? unit)
    (let [numerator (filter (comp pos? val) unit)
          denominator (->> unit
                           (filter (comp neg? val))
                           (map (fn [[baseunit exponent]]
                                  [baseunit (clojure.core/- exponent)])))
          base+exp->tex
          (fn [[base exp]]
            (str "\\operatorname{" (name base) "}"
                 (when (not= 1 exp)
                   (str "^{" exp "}"))))
          numerator-string (if (seq numerator)
                             (str/join " " (map base+exp->tex numerator))
                             "1")]
      (if-not (seq denominator)
        numerator-string
        (str "\\frac{" numerator-string "}"
             "{" (str/join " " (map base+exp->tex denominator)) "}")))))

(let [unit {:si/m -1 :si/s -1 :si/kg 1 :si/A 1}]
  (unit->tex unit))

(let [unit {:si/m -1 :si/s -1}]
  (unit->tex unit))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/example
 (unit->tex {:m 2 :s -1})
 (clerk/tex (unit->tex {:m 2 :s -1})))

;; That looks like what I had in mind!
;; We also want _numbers with SI units_.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn with-unit->tex [with-unit]
  (str (cond-> (.number with-unit)
         ratio? double)
       " "
       (unit->tex (.unit with-unit))))

;; For with-units (what a weird noun), the raw TeX and the rendered TeX look different:

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/example
 (with-unit->tex (WithUnit. 0.3 {:si/m 1}))
 (clerk/tex (with-unit->tex (WithUnit. 0.3 {:si/m 1})))
 )

;; Finally, we can create a viewer.

(do
  (def with-unit-viewer
    {:name `with-unit-viewer
     :pred with-unit?
     :transform-fn (clerk/update-val (fn [unit] (clerk/tex (with-unit->tex unit))))})

  (clerk/add-viewers! [with-unit-viewer])

  (WithUnit. 0.3 {:si/m 1}))

;; It's working!
;; Time to implement *.
;; We're going to use multimethods to support plain numbers numbers without units.

;; First, we need a dispatch fn for two-arg type-based multimethods.

^{:nextjournal.clerk/visibility {:result :hide}}
(defn ^:private both-types [a b]
  [(type a) (type b)])

(clerk/example
 (both-types 1 1)
 (both-types 1 (WithUnit. 0.3 {:si/m 1})))

;; Numbers with units are equal when the numbers are equal and the units are equal.
;; This normally works out fine:

(= (WithUnit. 0.3 {:si/m 1})
   (WithUnit. 0.3 {:si/m 1}))

;; But zero exponents give us trouble:

(= (WithUnit. 0.3 {:si/m 1})
   (WithUnit. 0.3 {:si/m 1 :si/s 0}))

^{:nextjournal.clerk/visibility {:code :hide :result :show}}
(let [unit (fn [base exponent]
             (format "\\mathrm{%s}^{%d}" (name base) exponent))
      equation (fn [& args]
                (str "$" (apply str args) "$"))]
  (clerk/md (str "But "
                 (equation (unit :m 0) " = " 1)
                 "!"
                 " Therefore "
                 (equation "0.3 \\times"
                           " " (unit :m 1)
                           " " (unit :s 0)
                           " = "
                           "0.3 \\times"
                           " " (unit :m 1)
                           " = "
                           "0.3 \\times"
                           " " "\\mathrm m")
                 "."
                 " We can solve that by simplifying away zero exponents in the exponent map.")))

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

;; Zero exponents simplify away:

(simplify (WithUnit. 0.3 {:si/m 1 :si/s 0}))

;; And if all exponents are zero, the number simplifies to a plain Clojure number:

(let [x (simplify (WithUnit. 0.3 {:si/m 0 :si/s 0}))]
  {:type (type x) :x x})

;; Then we implement a constructor in terms of the simplifier.

(defn with-unit [number unit]
  (simplify (WithUnit. number unit)))

;; If we always use `with-unit` and consider `WithUnit.` an implementation detail, equality will work as expected!

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

(multiply 100 (with-unit 0.3 {:si/m 1}))

(defn *
  ([a] a)
  ([a b] (multiply a b))
  ([a b & args] (reduce multiply (multiply a b) args)))

(multiply 100 (WithUnit. 0.3 {:si/m 1}))

(let [height (with-unit 0.3 {:si/m 1})]
  (clerk/example
   (* height 0.5)
   (* height height)))

;; ## Arithmetic for numbers with units

;; To divide, we use 1-arity inversion and then rely on our multiplication.

(do
  (defmulti invert type)

  (defmethod invert Number
    [a]
    (clojure.core// a))

  (defmethod invert WithUnit
    [a]
    (with-unit (clojure.core// (.number a))
      (update-vals (.unit a) clojure.core/-))))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/example
 (invert 3)
 (invert (with-unit 1 {:si/m 1})))

(defn /
  ([a] (invert a))
  ([a b] (* a (invert b)))
  ([a b & rest] (* a (invert b) (reduce * (map invert rest)))))

(let [kg (with-unit 1 {:si/kg 1})
      m (with-unit 1 {:si/m 1})
      s (with-unit 1 {:si/s 1})
      N (/ (* kg m)
           (* s s))
      kN (* 1000 N)
      mm (/ m 1000)
      mm2 (* mm mm)]
  (/ (* 80 kN)
     (* 300 mm2)))

;; let's factor that into MPa instead:

(let [kg (with-unit 1 {:si/kg 1})
      m (with-unit 1 {:si/m 1})
      s (with-unit 1 {:si/s 1})
      N (/ (* kg m)
           (* s s))
      kN (* 1000 N)
      mm (/ m 1000)
      mm2 (* mm mm)
      MPa (/ N mm2)]
  (/ (/ (* 80 kN)
        (* 300 mm2))
     MPa))

;; ## Steel beams with units

;; Above, we called this an IPE300 beam:

^{:nextjournal.clerk/visibility {:code :hide}}
{:r 15,
 :wy 557,
 :s 7.1,
 :prefix "IPE",
 :wz 80.5,
 :h 300,
 :b 150,
 :iz 6.04,
 :t 10.7,
 :iy 83.6,
 :profile 300,
 :a 5.38}

;; But what units do `:r`, `:wy` and `:iz` have?
;; Let's make a new map where values have SI units.

;; Meters, millimeters and square millimeters are convenient to define with multiplication:

(let [m (with-unit 1 {:si/m 1})
      mm (* 10e-3 m)
      mm2 (* mm mm)]
  (clerk/row m mm mm2))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn md-unit [base exponent]
  (format "$\\mathrm{%s}^{%d}$" (name base) exponent))

^{:nextjournal.clerk/visibility {:code :hide :result :show}}
(clerk/md (str "But for " (md-unit :mm 3) " and " (md-unit :mm 4) " using multiplication is annoying."
               " Let's fix that by defining exponentiation for WithUnit."))

;; The implementation is similar to `multiply`, except that we don't allow numbers with units _as exponents_.
;; We'll rely on clojure.math/pow under the hood.

(do
  (defmulti pow both-types)

  (defmethod pow [Number Number]
    [base exponent]
    (math/pow base exponent))

  (defmethod pow [Number WithUnit]
    [base exponent]
    (throw (ex-info "WithUnit as exponent is not supported"
                    {:base base :exponent exponent})))

  (defmethod pow [WithUnit Number]
    [base exponent]
    (with-unit
      (math/pow (.number base) exponent)
      (update-vals (.unit base) (partial * exponent))))

  (defmethod pow [WithUnit WithUnit]
    [base exponent]
    (throw (ex-info "WithUnit as exponent is not supported"
                    {:base base :exponent exponent}))))

;; Does it work as expected?

(let [m (with-unit 1 {:si/m 1})
      mm (* 10e-3 m)]
  (clerk/example mm
                 (* mm mm)
                 (pow mm 2)
                 (= (* mm mm) (pow mm 2))))

;; Looks all right to me!

;; Time to revisit our IPE beam.
;; This time, we add units.


^{:nextjournal.clerk/visibility {:code :hide}}
(let [m (with-unit 1 {:si/m 1})
      mm (* 10e-3 m)
      mm2 (pow mm 2)
      mm3 (pow mm 3)
      mm4 (pow mm 4)]
  {:r (* 15 mm),
   :wy (* 557 10e3 mm3),
   :s (* 7.1 mm),
   :prefix "IPE",
   :wz (* 80.5 10e3 mm3),
   :h (* 300 mm),
   :b (* 150 mm),
   :iz (* 6.04 10e6 mm4),
   :t (* 10.7 mm),
   :iy (* 83.6 10e6 mm4),
   :profile 300,
   :a (* 5.38 10e3 mm2)})

;; ## Thank you
;;
;; To Sam Ritchie, Martin Kavalar and Jack Rusher for making good tools,
;;   and for helping people who want to learn.
;; To Gerald Jay Sussman for improving the way we think about programming, and
;;   expanding the range of problems we can solve with programming.
;; To Eugene Pakhomov, Joshua Suskalo and Ethan McCue for helping me understand
;;   how Java types, Clojure multimethod type hierarchies are connected.

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
