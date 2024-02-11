;; # SQL Migrations in Clojure: understanding Ragtime
;;
;; - Scope: explain the principles of Ragtime
;;
;; - Target audience: familiar with Clojure, but the Ragtime readme isn't
;;   immediately explaining all wanted topics.
;;
;; - Personal motivation: want a nice database migration setup for
;;   Mikrobloggeriet.no, wanting to put some effort into understanding the
;;   basics.

(ns teodorlu.lab.migrations-understanding-ragtime
  (:require
   [nextjournal.clerk :as clerk]
   [ragtime.protocols]
   [ragtime.core :as ragtime]))

ragtime/migrate

;; Working through https://github.com/weavejester/ragtime/wiki/Concepts

(defrecord MemoryDatabase [data migrations]
  ragtime.protocols/DataStore
  (add-migration-id [_ id]
    (swap! migrations conj id))
  (remove-migration-id [_ id]
    (swap! migrations (partial filterv (complement #{id}))))
  (applied-migration-ids [_]
    (seq @migrations)))

(defn memory-database []
  (->MemoryDatabase (atom {}) (atom [])))

(defonce db1 (memory-database))
(-> db1 :data deref)
(ragtime.protocols/applied-migration-ids db1)

(def add-foo
  (reify ragtime.protocols/Migration
    (id [_] "add-foo")
    (run-up! [_ db] (swap! (:data db) assoc :foo 1))
    (run-down! [_ db] (swap! (:data db) dissoc :foo))))



^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
