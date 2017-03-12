(ns mia-work-to-png.core
  (:require [clojure.java.io :as io]
            [clojure.core.async :as asyc :refer [go <!]]
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

(defn best-size [pre-url size post-url]
  (go
    (if (= :ok (:status (<! (copy-uri-to-file (str pre-url size post-url)))))
      (best-size pre-url (inc size) post-url)
      (if (= size 3)
        {:status :error}
        {:status :ok :data (dec size)}))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [id (->> args first url :path split (nth 2) Integer.)]
    (println (<! (best-size "asd" "oaiwfm")))))
