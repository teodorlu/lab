;; # Write yourself a virtual file system

^{:nextjournal.clerk/toc true
  :nextjournal.clerk/visibility {:code :hide}}

(ns a-vfs)

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
