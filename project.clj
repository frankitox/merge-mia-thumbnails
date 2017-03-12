(defproject mia-work-to-png "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/core.async "0.2.395"]
                 [com.cemerick/url "0.1.1"]]
  :main ^:skip-aot mia-work-to-png.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
