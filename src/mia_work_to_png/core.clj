(ns mia-work-to-png.core
  (:require [clojure.java.io :as io]
            [clojure.core.async :as asyc :refer [go <! <!!]]
            [clojure.string :refer [split]]
            [cemerick.url :refer [url]])
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

(defn download-map [id size]
  (loop [prev-x nil x 0 y 1]
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
        size (:data (<!! (best-size id 3)))]
    (println "final" (download-map id size))))
