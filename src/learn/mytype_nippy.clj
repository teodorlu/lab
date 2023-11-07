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
