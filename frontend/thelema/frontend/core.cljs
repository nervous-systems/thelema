(ns ^:figwheel-always thelema.frontend.core
  (:require [reagent.core :as reagent]
            [cemerick.url :refer [url]]
            [thelema.frontend.util :as util]
            [reagent-forms.core :as forms]
            [glossop.core :as g :refer-macros [go-catching <?]]
            [cljs-http.client :as http]))

(enable-console-print!)

(def BASE-URL (url "https://aa5dhpxk10.execute-api.us-east-1.amazonaws.com/prod"))

(def app-state (reagent/atom {}))

(defn search [query app-state]
  (go-catching
    (swap! app-state assoc :results
           (<? (http/request
                {:method :get
                 :channel (util/response-chan)
                 :with-credentials? false
                 :url (-> BASE-URL
                          (url "audio" "search")
                          (assoc :query {:term query})
                          str)})))))

(defn render-app [app-state]
  (let [{{:keys [query]} :inputs :keys [results]} @app-state]
    [:h1 "Make a Playlist"
     [:div
      [forms/bind-fields
       [:input.form-control {:field :text :id :inputs.query}]
       app-state]
      [:button {:on-click #(search query app-state)} "Search"]]
     [:ol
      (for [{:keys [url title] :as video} results]
        [:li {:key url} title])]]))

(defn mount-root []
  (reagent/render-component
   [render-app app-state]
   (.getElementById js/document "app")))

(defn init []
  ;; ...
  (mount-root))

(init)
