(ns learn.mytype-nippy
  (:require
   [taoensso.nippy :as nippy]
   [clojure.edn :as edn]))

;; RECORDS

(defrecord MyRecord [data])

(nippy/extend-freeze MyRecord ::MyRecord
  [x data-output]
  (.writeUTF data-output (:data x)))

(nippy/extend-thaw :my-type/foo
  [data-input]
  (MyRecord. (.readUTF data-input)))

(nippy/thaw (nippy/freeze (MyRecord. "Joe")))
;; => {:data "Joe"}

;; TYPES?

(deftype MyType [data])

(nippy/extend-freeze MyType ::MyType
  [x data-output]
  (.writeUTF data-output (.data x)))

(nippy/extend-thaw ::MyType
  [data-input]
  (MyType. (.readUTF data-input)))

(nippy/thaw (nippy/freeze (MyType. "Joe")))

;; TYPES WITH TWO PARAMS

(deftype Point [x y])

(nippy/extend-freeze Point ::Point
  [p data-output]
  (.writeUTF data-output (pr-str [(.x p) (.y p)])))

(nippy/extend-thaw ::Point
  [data-input]
  (let [[x y] (edn/read-string (.readUTF data-input))]
    (Point. x y)))

(nippy/thaw (nippy/freeze (Point. 97 77)))

;; TWO PARAMS TAKE TWO

(deftype Point3 [x y z])

(nippy/extend-freeze Point ::Point3
  [p data-output]
  (.writeUTF data-output ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; what about Nippy's *freeze-serializable-allowlist* ?
;; Martin suggested looking into it.

(deftype P [x y])

(alter-var-root #'nippy/*thaw-serializable-allowlist*
                (fn [list] (conj list "learn.mytype-nippy.Point3")))

(let [p (-> (P. 1 2)
            nippy/freeze
            nippy/thaw)]
  (.y p))

(comment
  nippy/*freeze-serializable-allowlist*
  ;; => #{"*"}

  ;; nippy/*freeze-deserializable-allowlist*
  (sort
   nippy/*thaw-serializable-allowlist*)
  ;; => ("[B"
  ;;     "[C"
  ;;     "[D"
  ;;     "[F"
  ;;     "[I"
  ;;     "[J"
  ;;     "[S"
  ;;     "[Z"
  ;;     "clojure.lang.ArityException"
  ;;     "clojure.lang.ExceptionInfo"
  ;;     "clojure.lang.Namespace"
  ;;     "clojure.lang.Var"
  ;;     "java.io.File"
  ;;     "java.lang.ArithmeticException"
  ;;     "java.lang.ClassCastException"
  ;;     "java.lang.Exception"
  ;;     "java.lang.IllegalArgumentException"
  ;;     "java.lang.IndexOutOfBoundsException"
  ;;     "java.lang.NullPointerException"
  ;;     "java.lang.RuntimeException"
  ;;     "java.lang.Throwable"
  ;;     "java.net.URI"
  ;;     "java.time.Clock"
  ;;     "java.time.DateTimeException"
  ;;     "java.time.LocalDate"
  ;;     "java.time.LocalDateTime"
  ;;     "java.time.LocalTime"
  ;;     "java.time.MonthDay"
  ;;     "java.time.OffsetDateTime"
  ;;     "java.time.OffsetTime"
  ;;     "java.time.Year"
  ;;     "java.time.YearMonth"
  ;;     "java.time.ZoneId"
  ;;     "java.time.ZoneOffset"
  ;;     "java.time.ZonedDateTime"
  ;;     "org.joda.time.DateTime")
  )
