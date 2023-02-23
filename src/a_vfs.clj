;; # Write yourself a virtual file system

^{:nextjournal.clerk/toc true
  :nextjournal.clerk/visibility {:code :hide}}

(ns a-vfs
  (:require meld
            [nextjournal.clerk :as clerk])
  (:import (java.nio ByteBuffer)))
;; I'm working from this starting point by Jack Rusher:
;;
;; https://gist.github.com/jackrusher/e5fef18113ae721486c47acada19089c
;;
;; The VFS code uses `jnr-fuse`:
;;
;; https://github.com/SerCeMan/jnr-fuse

;; ## My motivation: dead simple file-based user interfaces
;;
;; What if the command interface for your blob just was a virtual file system?
;; And behind the scenes, everything "just worked"?
;;
;; That's the idea.
;; Run it, and it interacts with your system natively.

@meld/the-filesystem

@meld/ersatz-fs

(comment

  (swap! meld/ersatz-fs assoc "/posts" {:contents #{} :type :dir})
  (swap! meld/ersatz-fs update-in ["/" :contents] conj "posts")

  )

(defn create-root-folder! [name]
  (swap! meld/ersatz-fs
         (fn [fs]
           (-> fs
               (assoc (str "/" name) {:contents #{} :type :dir})
               (update-in ["/" :contents] conj name)))))

(create-root-folder! "mythings")
(create-root-folder! "items")

;; but, how do I create a Byte Buffer?


(defn str->bytes [s] (.getBytes s "UTF-8"))
(defn bytes->str [byte-arr] (String. byte-arr "UTF-8"))

(defn create-root-file! [name s]
  (swap! meld/ersatz-fs
         (fn [fs]
           (-> fs
               (assoc (str "/" name) {:contents (str->bytes s)
                                      :type :file})
               (update-in ["/" :contents] conj name)))))

(create-root-file! "message.txt" "hei fra teodor")

(slurp "/tmp/meld/message.txt")

#_ "Trailing whitespace"
^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html (into [:div (repeat 10 [:br])]))
