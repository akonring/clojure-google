(defproject training-planner "0.1.0-SNAPSHOT"
  :description "This application should retrieve training information from google docs and present them in the calendar"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2030"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojure/data.json "0.2.3"]
                 [org.clojure/data.xml "0.0.7"]
                 [clj-http "0.7.9"]
                 [bwhmather/clj-oauth2 "0.5.1" :exclusions [org.clojure/data.json]]
                 [ring "1.2.1"]
                 [compojure "1.1.6"]
                 [enlive/enlive "1.1.5"]
                 [cheshire "5.3.1"]
                 [clj-webdriver "0.6.0"]
                 [oauth-clj "0.1.9"]
                 [com.google.gdata/gdata-spreadsheet-3.0 "1.41.5"]
                 [clj-time "0.6.0"]
                 [org.clojure/data.csv "0.1.2"]]
  :repositories {"mandubian-mvn" "http://mandubian-mvn.googlecode.com/svn/trunk/mandubian-mvn/repository"}
  :main ^:skip-aot training-planner.core
  :target-path "target/%s"
:profiles {:uberjar {:aot :all}})
