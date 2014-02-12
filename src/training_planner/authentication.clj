(ns training-planner.authentication
  (:require
   [cheshire.core :refer [parse-string]]
   [oauth.google :as oauth]
   [clojure.pprint :refer [pprint]]))

(def client_id "936415890582-rfv5ibnbqs7ejtfgt7onpt7k5or5h6ne.apps.googleusercontent.com")

(def client_secret "jJiliqwp-si8D4lx3x6aZJdO")

(def redirect_uri "http://localhost:8080/callback")

(def scope {:spreadsheet "https://spreadsheets.google.com/feeds"})

(def oauth-url (oauth/oauth-authorization-url
                client_id
                redirect_uri
                :scope ["https://spreadsheets.google.com/feeds"]))

(defn get-access-token [auth-token]
  (let [access-token 
        (oauth/oauth-access-token client_id client_secret auth-token redirect_uri)]
        access-token))

(defn retrieve-token [request]
  (-> request
      (:params)
      (:code)
      (get-access-token)))

;; Higher-order function that returns a client that can interact with google
(defn oauth-client [access-token]
  (oauth/oauth-client access-token))


