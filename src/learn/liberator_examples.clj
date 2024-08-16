;; # Liberator examples

(ns learn.liberator-examples
  {:nextjournal.clerk/toc true}
  (:require
   [nextjournal.clerk :as clerk]
   [liberator.core :as liberator]
   [liberator.util]
   [ring.mock.request :refer [request]]))

;; ## Setup
;;
;; We base this on the Liberator test suite, but that code requires some setup.

{:nextjournal.clerk/visibility {:result :hide}}

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

{:nextjournal.clerk/visibility {:result :show}}

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

{:nextjournal.clerk/auto-expand-results? true}

;; ## Goal: understanding how :allowed-methods, :allowed and :put! work together
;;
;; How do these three interact?
;; How can I use that?
;;
;; Let's put it to work.

#{:allowed-methods :allowed? :put!}

;; ## `:allowed-methods` sets a whitelist of which HTTP responses you want to handle.

;; An HTTP request with an allowed request method gives a 2xx response.

((resource {:allowed-methods [:get]})
 (request :get "/"))

((resource {:allowed-methods [:put]})
 (request :put "/"))

;; An HTTP request with a disallowed request method gives a 4xx response.

((resource {:allowed-methods [:put]})
 (request :get "/"))

;; ## `:allowed`
;;
;; With `:allowed true`, I expect to get `:status 200`.

((resource {:allowed true})
 (request :get "/"))

;; With `:allowed false`, I expect to get `:status 4xx`.

((resource {:allowed false})
 (request :get "/"))

;; No!
;; Hmm, weird.
;; What about a function?

((resource {:allowed (constantly false)})
 (request :get "/"))

;; 🤦
;;
;; It's `:allowed?`, not `:allowed`.
;; We try again.

(clerk/example
  ((resource {:allowed? true}) (request :get "/"))
  ((resource {:allowed? false}) (request :get "/"))
  ((resource {:allowed? (constantly false)}) (request :get "/")))

;; Here we go, that's more like it.

;; What arguments get passed to the `:allowed?` function?

(let [allowed-arg (promise)]
  ((resource {:allowed? #(deliver allowed-arg %)}) (request :get "/"))
  @allowed-arg)

;; there we go, lots.
;; Let's get just the keys.

(clerk/example
  (let [allowed-arg (promise)]
    ((resource {:allowed? #(deliver allowed-arg %)}) (request :get "/"))
    (-> @allowed-arg :resource keys sort))

  ((resource {:allowed? (constantly [true {::x 33}])
              :handle-ok (fn [c] (pr-str {:x (::x c)}))})
   (request :get "/"))

  (let [state (atom {})]
    (swap! state assoc :response
           ((resource {:allowed-methods #{:put}
                       :allowed? (fn [allowed-args]
                                   (swap! state assoc :allowed-args allowed-args)
                                   [true {::x 33}])
                       :put! (fn [put-args]
                               (swap! state assoc :put-args put-args)
                               "PUT IT")})
            (request :put "/")))
    @state))

;; ## Task: pass an argument from :allowed? to the return value.

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
