;; # Liberator examples

(ns learn.liberator-examples
  {:nextjournal.clerk/toc true}
  (:require
   [nextjournal.clerk :as clerk]
   [liberator.core :as liberator]
   [ring.mock.request]))

;; ## Setup

(let [targets ["decisions" "actions" "handlers"]]
  (clerk/row (for [target targets
                   :let [href (format "http://clojure-liberator.github.io/liberator/doc/%s.html" target)]]
               (clerk/html [:a {:href href} (str "Liberator " target)])) ))

(defn millis-after-epoch [millis]
  (java.util.Date. millis))

(defn if-modified-since [req value]
  (ring.mock.request/header req "if-modified-since" value))

(defn if-unmodified-since [req value]
  (ring.mock.request/header req "if-unmodified-since" value))

(defn if-match [req value]
  (ring.mock.request/header req "if-match" (if (= "*" value) value (str "\"" value "\""))))

(defn if-none-match [req value]
  (ring.mock.request/header req "if-none-match" (if (= "*" value) value (str "\"" value "\""))))

(defn resource [kvs]
  (fn [request]
    (liberator/run-resource request kvs)))

;; ## `:last-modified`
;;
;; returns a 304 if the client has a more recent version than the last modified server version

((resource {:exists? true
            :handle-ok "OK"
            :last-modified (millis-after-epoch 1001)})
 (-> (ring.mock.request/request :get "/")
     (if-modified-since (liberator.util/http-date (millis-after-epoch 1000)))))

((resource {:exists? true
            :handle-ok "OK"
            :last-modified (millis-after-epoch 999)})
 (-> (ring.mock.request/request :get "/")
     (if-modified-since (liberator.util/http-date (millis-after-epoch 1000)))))
