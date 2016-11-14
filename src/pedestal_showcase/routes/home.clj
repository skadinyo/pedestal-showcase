(ns pedestal-showcase.routes.home
  (:require
   [io.pedestal.interceptor :refer [interceptor]]
   [io.pedestal.http :as http]
   [ring.util.response :as r.resp]
   [pedestal-showcase.pedestal :as i.p]
   [hiccup.core :as h.c]))

(defn body
  [context content]
  [:div
   [:ul
    [:li
     [:a {:href "/"} "home"]]
    [:li
     [:a {:href "/sign-in"} "sign-in"]]
    [:li
     [:a {:href "/log-out"} "log-out"]]
    [:li
     [:a {:href "/showcases"} "showcases"]]]
   [:div [:h3 (str context)]]
   [:div
    content]])

(def home-page
  (interceptor
   {:name ::home-page
    :enter (fn [{:keys [request] :as context}]
             (let [session (:session request)]
               (-> context
                   (assoc :response
                          (r.resp/response
                           (h.c/html
                            (body
                             {:session session}
                             [:h1 "home"])))))))}))

(def sign-in-page
  (interceptor
   {:name ::home-page
    :enter (fn [{:keys [request] :as context}]
             (let [session (:session request)]
               (-> context
                   (assoc :response
                          (r.resp/response
                           (h.c/html
                            (body
                             {:session session}
                             [:div
                              [:h1 "sign-in"]
                              [:form 
                               {:action "/sign-in"
                                :method "post"}
                               "User:" 
                               [:br]
                               [:input {:type "text", :name "user", :value "Mickey"}]
                               [:br]"Pass:" 
                               [:br]
                               [:input {:type "password"
                                        :name "pass"}]
                               [:br]
                               [:br]
                               [:input
                                {:type "submit"
                                 :value "Submit"}]]])))))))}))

(def handle-login
  (interceptor
   {:name ::handle-login
    :enter (fn [{:keys [request] :as context}]
             (let [{:keys [user pass]} (:form-params request)]
               (-> context
                   (assoc :response 
                          (-> (r.resp/redirect "/")
                              (assoc :session 
                                     {:user user
                                      :pass pass}))))))}))

(def log-out-page
  (interceptor
   {:name ::home-page
    :enter (fn [{:keys [request] :as context}]
             (-> context
                 (assoc :response
                        (-> (r.resp/redirect "/")
                            (assoc :session nil)))))}))


(def showcase-page
  (interceptor
   {:name ::showcase-page
    :enter 
    (fn [{:keys [request] :as context}]
      (let [session (:session request)]
        (-> context
            (assoc :response
                   (r.resp/response
                    (h.c/html
                     (body
                      {:session session}
                      [:ul
                       [:li 
                        [:a {:href "/api/json"}
                         "json-response"]]
                       [:li 
                        [:a {:href "/api/transit"}
                         "transit-response"]]
                       [:li 
                        [:a {:href "/api/pemangkat/2"}
                         "path-string-colon"]]
                       [:li 
                        [:a {:href "/api/uppercase/abcde/momo/CINA"}
                         "wild-card-route"]]
                       [:li 
                        [:a {:href "/api/component"}
                         "component-example"]]
                       ])))))))}))

(def middlewares
  [])

(def home-routes
  [["/" :get [home-page] :route-name :home-page]
   ["/showcases" :get [showcase-page] :route-name :showcase-page]
   ["/sign-in" :get [sign-in-page] :route-name :sign-in-page]
   ["/sign-in" :post [handle-login] :route-name :handle-sign-in]
   ["/log-out" :get [log-out-page] :route-name :log-out-page]])

(defn make-home-routes
  [config]
  (->> home-routes
       (mapv (fn [r]
              (update r 2 #(vec (concat middlewares %)))))))
