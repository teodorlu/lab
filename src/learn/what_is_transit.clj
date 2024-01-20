(ns learn.what-is-transit
  (:require [cognitect.transit :as transit])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

;; what is transit??

(defn ->transit-json-str [x]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer x)

    ;; Take a peek at the JSON
    (.toString out)))

(->transit-json-str {:a "b" })

"[\"^ \",\"~:a\",\"b\"]"
