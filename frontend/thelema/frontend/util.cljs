(ns thelema.frontend.util
  (:require [cljs.core.async :as async]))

(defn unwrap-response [{:keys [body status] :as resp}]
  (if (<= 200 status 299)
    body
    (ex-info "HTTP error" req)))

(def response-chan (partial async/chan 1 (map unwrap-response)))
