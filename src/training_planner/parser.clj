(ns training-planner.parser
  (:require 
   [clojure.pprint :refer [pprint]]
   [clojure.xml :as xml1]
   [training-planner.authentication :as auth])
  (:import 
   (java.io ByteArrayInputStream)))

(defn list-docs [access-token]
  ((auth/oauth-client access-token) 
   {:method :get
    :url "https://spreadsheets.google.com/feeds/spreadsheets/private/full"}))

;; Thanks to Solo labs
(defn get-struct-map [xml]
  (let [stream (ByteArrayInputStream. (.getBytes (.trim xml)))]
    (xml1/parse stream)))

(defn find-tag [val map]
  "Does the tag entry have value val?"
  (= (:tag map) val))

(defn filter-entries [xml]
  (let [content (:content xml)]
    (filter #(find-tag :entry %) content)))

(defn find-title [entry]
  (let [content (:content entry)
        record (first (filter #(find-tag :title %) content))]
    (first (:content record))))

(defn find-link [entry]
  (let [content (:content entry)
        record (second (filter #(find-tag :link %) content))]
    (-> record
        (:attrs)
        (:href))))

(defn entry-record [entry]
  {:title (find-title entry)
   :href (find-link entry)})

(defn entries-list [xml]
  (map entry-record (filter-entries xml)))

(defn get-doc-list-pairs [access-token]
  (do
    (pprint access-token)
  (-> (list-docs access-token)
   (get-struct-map)
   (entries-list))))


;; Retrieving the calendar feed from Spreadsheet


