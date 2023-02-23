(ns meld
  (:import (ru.serce.jnrfuse ErrorCodes FuseStubFS)
           (ru.serce.jnrfuse.struct FileStat)
           (java.io File)
           (java.nio.file Paths)
           (java.nio ByteBuffer))
  (:gen-class))

;; Based on https://gist.github.com/jackrusher/e5fef18113ae721486c47acada19089c
;; by Jack Rusher

;; error codes
(defonce enoent-error  (* -1 (ErrorCodes/ENOENT)))
(defonce eisdir-error  (* -1 (ErrorCodes/EISDIR)))
(defonce enotdir-error (* -1 (ErrorCodes/ENOTDIR)))

(comment
  (ErrorCodes/ENOENT))

;; the backing store for our ersatz filesystem
(def ersatz-fs
  (atom {"/" {:type :dir
              :contents #{"notebooks"}}
         "/notebooks" {:type :dir
                       :contents #{"rubbish.nb"}}
         ;; empty file
         "/notebooks/rubbish.nb" {:type :file
                                  :contents (ByteBuffer/allocate 0)}}))

;; TODO ctime, mtime, atime
(defn getattr-directory [ctx path stat]
  (doto stat
    (-> .-st_mode (.set (bit-or FileStat/S_IFDIR 0755)))
    (-> .-st_nlink (.set 2))
    (-> .-st_uid (.set (.get (.uid ctx))))
    (-> .-st_gid (.set (.get (.gid ctx))))))

;; TODO ctime, mtime, atime
(defn getattr-file [ctx path stat]
  (doto stat
    (-> .-st_mode (.set (bit-or FileStat/S_IFREG 0644)))
    (-> .-st_size (.set (.capacity (:contents (@ersatz-fs path)))))
    (-> .-st_nlink (.set 1))
    (-> .-st_uid (.set (.get (.uid ctx))))
    (-> .-st_gid (.set (.get (.gid ctx))))))

(defn readdir-list-files [path buf filt offset fi entries]
  (.apply filt buf "." nil 0)
  (.apply filt buf ".." nil 0)
  (doseq [entry entries]
    (.apply filt buf entry nil 0))
  filt)

;; NB Assumes the entry is already locked
(defn read-fuse-file [path buf size offset fi]
  (let [contents (:contents (@ersatz-fs path))
        read-size (min (- (.capacity contents) offset) size)
        bytes-read (byte-array read-size)]
    (-> contents
        (.position offset)
        (.get bytes-read 0 read-size)
        (.position 0))
    (.put buf 0 bytes-read 0 read-size)
    read-size))

(defn dirname [path]
  (clojure.string/replace path #"/[^/]*$" ""))

(defn basename [path]
  (second (re-find #"/([^/]*)$" path)))

(defn make-fuse-filesystem []
  (proxy [FuseStubFS] []
    (getattr [path stat]
      (println "getattr: " path)
      (let [ctx (proxy-super getContext)]
        (if-let [ent (@ersatz-fs path)]
          (locking ent
            (if (= :dir (:type ent))
              (getattr-directory ctx path stat)
              (getattr-file ctx path stat)))
          enoent-error)))

    (readdir [path buf filt offset fi]
      (println "readdir: " path)
      (if-let [ent (@ersatz-fs path)]
        (locking ent
          (if (= :dir (:type ent))
            (readdir-list-files path buf filt offset fi (sort (:contents (@ersatz-fs path))))
            enotdir-error))
        enoent-error))

    (open [path fi] ; NB create/truncate handled automatically
      (println "open: " path)
      (if (@ersatz-fs path)
        0
        enoent-error))

    (read [path buf size offset fi]
      (println "read: " path)
      (if-let [ent (@ersatz-fs path)]
        (locking ent
          (if (= :dir (:type ent))
            eisdir-error
            (read-fuse-file path buf size offset fi)))
        enoent-error))

    (create [path mode fi]
      (println "create: " path)
      (if-let [dir (@ersatz-fs (dirname path))]
        (locking dir
          (swap! ersatz-fs #(-> %
                                (update-in [(dirname path) :contents] conj (basename path))
                                (assoc path {:type :file :contents (ByteBuffer/allocate 0)}))))
        enoent-error))

    (write [path buf size offset fi]
      (println "write: " path)
      (if-let [ent (@ersatz-fs path)]
        (locking ent
          (if (= :dir (:type ent))
            eisdir-error
            (let [max-extent (+ size offset)
                  current-contents (:contents ent)
                  contents (if (< (.capacity current-contents) max-extent)
                             (let [new-contents (doto (ByteBuffer/allocate max-extent)
                                                  (.put current-contents))]
                               (swap! ersatz-fs assoc-in [path :contents] new-contents)
                               new-contents)
                             current-contents)
                  new-bytes (byte-array size)]
              (.get buf 0 new-bytes 0 size) ; load bytes into new array
              (-> contents
                  (.position offset)
                  (.put new-bytes)
                  (.position 0)) ; rewind
              size)))
        enoent-error))

    (truncate [path size]
      (println "truncate: " path ", size: " size)
      (if-let [ent (@ersatz-fs path)]
        (locking ent
          (if (= :dir (:type ent))
            eisdir-error
            (let [current-contents (:contents ent)]
              (when (> (.capacity current-contents) size)
                (let [new-bytes (byte-array size)]
                  (.get current-contents new-bytes)
                  (swap! ersatz-fs assoc-in [path :contents] (doto (ByteBuffer/allocate size)
                                                               (.put new-bytes)))))
              size)))
        enoent-error))

    (unlink [path]
      (println "unlink: " path)
      (if-let [ent (@ersatz-fs path)]
        (locking ent
          (if (= :dir (:type ent))
            eisdir-error
            (swap! ersatz-fs #(-> %
                                  (update-in [(dirname path) :contents] disj (basename path))
                                  (dissoc path)))))
        enoent-error))))

;; As an exercise to the reader, consider implementing some of these
;; other parts of the FUSE API:

;; mkdir, rmdir, statfs, rename mknod readlink symlink link chmod
;; chown flush release fsync opendir releasedir fsyncdir init destroy
;; access fgetattr lock flock fallocate utimens bmap ioctl poll
;; write_buf read_buf setxattr listxattr removexattr getxattr

;; API function signatures can be found here:
;; https://github.com/SerCeMan/jnr-fuse/blob/master/src/main/java/ru/serce/jnrfuse/FuseFS.java

(def the-filesystem (atom nil))

(defn mount! [mountpoint]
  (future
    (reset! the-filesystem (make-fuse-filesystem))
    ;; args to mount: this path blocking? debug? options -- this is
    ;; currently set to block, so don't call it interactively!
    (.mount @the-filesystem (-> mountpoint File. .toURI Paths/get) true false (into-array String []))))

(defn umount! []
  (.umount @the-filesystem))

;; catches CTRL-C and cleans up the mountpoint
(defn cleanup-hooks [mountpoint]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (println "Unmounting " mountpoint)
                               (umount!)))))

;; Currently a mostly blank stub that runs the filesystem
(defn run [opts]
  (println "Mounting: " (:mountpoint opts))
  (cleanup-hooks (:mountpoint opts))
  (deref (mount! (:mountpoint opts))))

(comment
  ;; repl helpers -- Teodor

  (mount! {:mountpoint "/tmp/meld"})
  (umount!)

  (deref the-filesystem)

  (future
    (run {:mountpoint "/tmp/meld"}))
  )
