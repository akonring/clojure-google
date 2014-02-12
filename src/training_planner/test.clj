(ns training-planner.test
  (:require
   [training-planner.authentication :as auth]
   [training-planner.parser :as parser]
   [training-planner.server :as server]
   [clj-http.client :as client]
   [clj-time.core :as time]
   [clj-time.format :as format]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io])
  (:import
   (java.net URL URI)
   (com.google.gdata.data.spreadsheet
    SpreadsheetFeed CellFeed)
   (com.google.gdata.client.spreadsheet
    SpreadsheetService)))

(def SPREADSHEET_FEED_URL
  (URL. "https://spreadsheets.google.com/feeds/spreadsheets/private/full"))

(defn get-workbook-cells-url [sheet-id book-id]
  (str "http://spreadsheets.google.com/feeds/cells/"
       sheet-id "/" book-id "/private/values"))

(def username "anders.konring@gmail.com")
(def password "koNy2771")

(defn get-service [username password name]
  "return Spreadsheet service instance already authenticated, may throw
  an exception if credentials are invalid"
  (let [service (SpreadsheetService. name)]
    (.setUserCredentials service username password)
    service))

(defn list-files [service]
  "get a list of all spreadsheet documents"
  (let [feed (.getFeed service SPREADSHEET_FEED_URL SpreadsheetFeed)
        entries (.getEntries feed)]
    entries))

(defn- parse-int [value]
  (Integer/parseInt value))

(defn- get-last-id-part [id]
  (.substring id (+ (.lastIndexOf id "/") 1)))

(defn get-id [thing]
  "return the id of the thing as a string"
  (get-last-id-part (.getId thing)))

(defn get-title [thing]
  "return the title of the thing as a string"
  (-> thing (.getTitle) (.getPlainText)))

(defn to-cell [cell]
  "return a clojure friendly cell representation from the original object"
  (let [id (get-id cell)
        coords (map parse-int
                    (clojure.string/split (subs id 1) #"C"))]
        {:id id
         :updated (.getUpdated cell)
         :coords coords
         :row (first coords)
         :col (second coords)
         :address (get-title cell)
         :input-value (-> cell (.getCell) (.getInputValue))
         :value (-> cell (.getCell) (.getValue))}))

(defn get-cells-from-feed-url [service feed-url-str min-row min-col max-row
                               max-col]
  "return the cells from a worksheet using a feed url"
  (let [cell-feed-url-str (str feed-url-str
                               "?min-row=" min-row
                               "&min-col=" min-col
                               "&max-row=" max-row
                               "&max-col=" max-col)
        cell-feed-url (.toURL (URI. cell-feed-url-str))
        cell-feed (.getFeed service cell-feed-url CellFeed)
        entries (.getEntries cell-feed)]
    (map to-cell entries)))

(defn get-cells [service worksheet min-row min-col max-row max-col]
  "get cells from a worksheet using a worksheet object"
  (let [feed-url-str (-> worksheet (.getCellFeedUrl) (.toString))
        entries (get-cells-from-feed-url service feed-url-str min-row min-col
                                         max-row max-col)]
    entries))

(defn get-worksheets [file]
  "get a list of all worksheets from a document"
  (.getWorksheets file))

(defn get-worksheets [file]
  "get a list of all worksheets from a document"
  (.getWorksheets file))

(defn search-by-title [title docs]
  (first (filter #(= (get-title %) title) docs)))

(def custom-formatter (format/formatter "MM/dd/yyyy"))


;; This is a basic ClientLogin using 
;; java interop.
;; Thanks to marianoguerra for creating the template

;; Returns a service which can be used to interact with
;; google spreadsheet

(def service (get-service username password "akonring"))

(def docs (list-files service))

(def week-plan (search-by-title "Ugeskabelon" docs))

(def current-worksheet (first (get-worksheets week-plan)))

(def cells (get-cells service current-worksheet 2 1 75 2))

;; Get a list of pairs from the sheet
;; (SesssionDate, SessionDescription)

(defn session-pairs [cells]
  (let [cell-rows (group-by :row 
                           (map #(select-keys % [:row :value]) cells))
        sessions (map second cell-rows)
        session-pairs (filter #(> (count %) 1) sessions)]
    session-pairs))

(defn pairs-to-vector [pairs]
  (let [csv-vector (map #(map :value %) pairs)]
    (map #(conj % "06:00") csv-vector)))

(defn create-csv [vector]
  (let [feed (cons ["Start Time","Start Date","Subject",]
                     vector)]
    feed))

(with-open [out-file (io/writer "out-files.csv")]
  (csv/write-csv out-file (create-csv 
                           (pairs-to-vector 
                            (session-pairs cells)))))


