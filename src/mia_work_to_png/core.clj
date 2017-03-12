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

(defn -main
  [& args]
  (let [id (-> args first url :path (split #"\/") (nth 2) Integer.)]
    (println (<!! (best-size id 3)))))
