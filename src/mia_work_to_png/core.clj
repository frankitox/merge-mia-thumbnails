(ns mia-work-to-png.core
  (:require [clojure.java.io :as io]
            [clojure.core.async :as asyc :refer [go <! <!!]]
            [clojure.string :refer [split]]
            [cemerick.url :refer [url]])
  (:import [javax.imageio ImageIO]
           [java.io File]
           [java.awt.image BufferedImage]
           [java.awt Color Graphics2D])
  (:gen-class))

(defn copy-uri-to-file [uri file]
  (go
    (try
      (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
        (io/copy in out) {:status :ok})
      (catch Exception e {:status :error}))))

(defn thumbnail-url [id size x y]
  (str "https://a.tiles.dx.artsmia.org/" id "/" size "/" x "/" y ".png"))

(defn gen-filename [id x y]
  (str "/tmp/" id "-" x "-" y ".png"))

(defn best-size [id size]
  (let [url (thumbnail-url id size 0 0)
        filename (gen-filename id 0 0)]
    (go
    (if (= :ok (:status (<! (copy-uri-to-file url filename))))
      (<! (best-size id (inc size)))
      (if (= size 3)
        {:status :error}
        {:status :ok :data (dec size)})))))

(defn merge-images
  ([one two] (merge-images one two nil))
  ([one two vertically?]
    (let [w (+ (.getWidth one) (if vertically? (.getWidth two) 0))
          h (+ (.getHeight one) (if vertically? 0 (.getHeight two)))
          img (BufferedImage. w h (BufferedImage/TYPE_INT_ARGB))
          g2 (.createGraphics img)
          color (.getColor g2)]
      (doto g2 (.setPaint Color/WHITE)
               (.fillRect 0 0 w h)
               (.setColor color)
               (.drawImage one nil 0 0)
               (.drawImage two nil
                           (if (not vertically?) (.getWidth one) 0)
                           (if vertically? (.getHeight one)))
               (.dispose))
      img)))
(defn horizontal-merge [left right]
  (let [w (+ (.getWidth left) (.getWidth right))
        h (.getHeight left)
        img (BufferedImage. w h (BufferedImage/TYPE_INT_ARGB))
        g2 (.createGraphics img)
        color (.getColor g2)]
    (doto g2 (.setPaint Color/WHITE)
             (.fillRect 0 0 w h)
             (.setColor color)
             (.drawImage left nil 0 0)
             (.drawImage right nil (.getWidth left) 0)
             (.dispose))
    img))
; http://stackoverflow.com/questions/20826216/copy-two-buffered-image-into-one-image-side-by-side

(defn download-map [id size]
  (loop [prev-x nil x 1 y 0]
    (do (println "looping with" x y)
     (let [url (thumbnail-url id size x y)
           filename (gen-filename id x y)
           copy (<!! (copy-uri-to-file url filename))]
      (if (= :ok (:status copy))
        (recur x (inc x) y)
        (if (and (= x 0) (> y 0))
          [(dec prev-x) (dec y)]
          (recur x 0 (inc y))))))))

(defn -main
  [& args]
  (let [id (-> args first url :path (split #"\/") (nth 2) Integer.)
        size (:data (<!! (best-size id 3)))
        left (ImageIO/read (File. (gen-filename id 0 0)))
        right (ImageIO/read (File. (gen-filename id 1 0)))]
    #_(download-map id size)
    (ImageIO/write (merge-images left right) "png" (File. "/tmp/lalal.png"))
    #_(println "final" (download-map id size))))
