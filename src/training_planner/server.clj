(ns training-planner.server
  (:require
   [ring.adapter.jetty :refer [run-jetty]]
   [compojure.handler :refer [site]]
   [compojure.core :refer [GET POST defroutes]]
   [compojure.route :refer [not-found resources]]
   [net.cgrand.enlive-html :as html]
   [training-planner.authentication :as auth]
   [clojure.pprint :refer [pprint]]
   [ring.util.response :refer [redirect]]
   [ring.middleware.nested-params :as parameters]
   [ring.middleware.session :as session]
   [ring.middleware.cookies :as cookies]
   [training-planner.parser :as parser]))

;; Hiccup is nice. Like haml you don't have
;; to end your tags and keep track of your div's
;; This is a snippet instead of creating a
;; a one-line html-file

(def html-snippet
  (html/html [:tbody [:tr [:td] [:td [:a {:href "#"}]]]])) 


(defn get-key-from-url [url]
  (let
      [handler identity
      temp-req {:params url}]
    (parameters/wrap-nested-params handler)
      temp-req))

;; Creating the snippet that "loops" through
;; the seq of docs and writing a list

(html/defsnippet docs-snippet html-snippet [:tbody] 
  [docs]
  [:tr] 
  (html/clone-for [{title :title url :href} docs]
                  [[:tr] [[:td (html/nth-of-type 1)]]] 
                  (html/content title)
                  [[:tr] [:td] [:a]]
                  (html/do->
                   (html/set-attr :href (str "/doc/" url))
                   (html/content "Load Calendar"))))
                  ;; [[:td (html/nth-of-type 2)]]
                  ;; (html/do->
                  ;;  (html/set-attr :href (str "/doc/" url))



;; Using a template to show the list of docs

(html/deftemplate docs-template "templates/docs.html"
  [docs]
  [:table] (html/content (docs-snippet docs)))


;; Another template for the welcome page
;; (could have used the same but this is just
;; a test example)

(html/deftemplate main-template "templates/index.html"
  [address]
  [:a] (html/set-attr "href" address))


;; Compojure's routing functionality
;; I love the functional approach
;; of Ring and Compojure

(defroutes app*
  ;; files in resources is accessible in root
  (resources "/")
  ;; Welcome page which is generating url to
  ;; Google's oauth endpoint
  (GET "/" [] (main-template auth/oauth-url))
  ;; Redirected to callback from google
  ;; making a request to google to obtain 
  ;; access-token and the interacting 
  ;; with google-api to obtain list 
  ;; of documents for the user
  (GET "/list" {cookies :cookies params :params}
       (let [doc-pairs (parser/get-doc-list-pairs (:value (cookies "token")))
             output (docs-template doc-pairs)]
         output))
  (GET "/callback" request
       (let
           [access-token (:access-token (auth/retrieve-token request))]
         (assoc (redirect "/list") :cookies {"token" access-token})))
  
  (GET "/doc/:id" {cookies :cookies params :params}
       params)
       
  ;; Nice compojure
  (not-found "Sorry, there's nothing here. Go away!"))

(defn wrap-spy [handler]
  (fn [request]
    (println "-------------------------------")
    (println "Incoming Request:")
    (clojure.pprint/pprint request)
    (let [response (handler request)]
      (println "Outgoing Response Map:")
      (clojure.pprint/pprint response)
      (println "-------------------------------")
      response)))
  

;; Ring wrapping the app with params
;; and session wrapping

;; Requstest --> show-params --> show-session-data --> (APP Routing)
;;                                                          | 
;;                                                         \/      
;; Response <--                                        (Handler)

(def app
  (-> app*
      (wrap-spy)
      (site)
      (wrap-spy)))


;; The jetty-server uses a reference to the app-handler

(defonce server (run-jetty #'app {:port 8080 :join? false}))
