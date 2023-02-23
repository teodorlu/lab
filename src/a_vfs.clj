;; # Write yourself a virtual file system

^{:nextjournal.clerk/toc true
  :nextjournal.clerk/visibility {:code :hide}}

(ns a-vfs
  (:require meld))

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
  (defn create-root-folder! [name]
    (swap! meld/ersatz-fs
           (fn [fs]
             (-> fs
                 (assoc (str "/" name) {:contents #{} :type :dir})
                 (update-in ["/" :contents] conj name)))))

  (create-root-folder! "mythings")
  (create-root-folder! "items")

  (swap! meld/ersatz-fs assoc "/posts" {:contents #{} :type :dir})
  (swap! meld/ersatz-fs update-in ["/" :contents] conj "posts")

  )
