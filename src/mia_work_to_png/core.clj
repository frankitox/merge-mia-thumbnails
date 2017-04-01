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
    (let [w (+ (.getWidth one) (if vertically? 0 (.getWidth two)))
          h (+ (.getHeight one) (if vertically? (.getHeight two) 0))
          img (BufferedImage. w h (BufferedImage/TYPE_INT_ARGB))
          g2 (.createGraphics img)
          color (.getColor g2)]
      (doto g2 (.setPaint Color/WHITE)
               (.fillRect 0 0 w h)
               (.setColor color)
               (.drawImage one nil 0 0)
               (.drawImage two nil
                           (if vertically? 0 (.getWidth one))
                           (if vertically? (.getHeight one) 0))
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
    (do
     (let [url (thumbnail-url id size x y)
           filename (gen-filename id x y)
           copy (<!! (copy-uri-to-file url filename))]
      (if (= :ok (:status copy))
        (do
          (println "Just downloaded file" filename)
          (recur x (inc x) y))
        (if (and (= x 0) (> y 0))
          [(dec prev-x) (dec y)]
          (recur x 0 (inc y))))))))

(defn -main [& args]
  (let [id (-> args first url :path (split #"\/") (nth 2) Integer.)
        size (:data (<!! (best-size id 3)))
        [max-x max-y] (download-map id size)
        _ (println "About to merge all the images")
        filenames (map (fn [y] (map (fn [x]
                                       (gen-filename id x y))
                                (range (inc max-x))))
                        (range (inc max-y)))
        thumbnails (map (fn [row] (map #(ImageIO/read (File. %)) row)) filenames)
        image (reduce (fn [whole part] (merge-images whole part :vertically))
                      (map (partial reduce merge-images) thumbnails))]
    (ImageIO/write image "png" (File. (second args)))
    (doseq [f (apply concat filenames)] (io/delete-file f))
    (println (str "Final image saved"))))
