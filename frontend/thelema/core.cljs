(ns thelema.core
  (:require [reagent.core :as reagent]))

(enable-console-print!)

(def app-state (reagent/atom {}))

(defn render-app [app-state]
  [:h1 (str "Hi there! " (.getTime (js/Date.)))])

(defn mount-root []
  (reagent/render-component
   [render-app app-state]
   (.getElementById js/document "app")))

(defn init []
  ;; ...
  (mount-root))

(init)
