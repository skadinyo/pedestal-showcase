(ns pedestal-showcase.routes.api
  (:require
   [io.pedestal.interceptor :refer [interceptor]]
   [io.pedestal.interceptor.chain :refer [terminate]]
   [io.pedestal.http :as http]
   [ring.util.response :as r.resp]
   [pedestal-showcase.pedestal :as p.p]
   [hiccup.core :as h.c]))

(def authenticate
  (interceptor
   {:name ::autheticate
    :enter (fn [{:keys [request] :as context}]
             (let [{:keys [user pass]} (:session request)]
               (if (and user pass)
                 context
                 (-> context
                     terminate
                     (assoc :response 
                            (r.resp/response
                             "put user and pass first. <a href=\"/sign-in\">sign-in</a>"))))))}))

(def inject-response-with-session
  (interceptor
   {:name ::inject-response-with-session
    :leave (fn [{:keys [request] :as context}]
             (-> context
                 (assoc-in [:response :body :session]
                           (:session request))))}))

(def middlewares
  [authenticate])

(def json-example
  (interceptor
   {:name ::json-example
    :enter (fn [{:keys [request] :as context}]
             (let [resp {:status 200
                         :body {:kcuf "uoy"}
                         :headers {}}]
               (assoc context :response resp)))}))

(def transit-example
  (interceptor
   {:name ::transit-example
    :enter (fn [{:keys [request] :as context}]
             (let [resp {:status 200
                         :body {:momo "sina"}
                         :headers {}}]
               (assoc context :response resp)))}))

(def pemangkat
  (interceptor
   {:name ::pemangkat
    :enter (fn [{:keys [request] :as context}]
             (let [x (-> request
                         :path-params
                         :x
                         bigint)
                   resp {:status 200
                         :body {:x (str x)
                                :x2 (str (* x x))}
                         :headers {}}]
               (assoc context :response resp)))
    }))

(def upper
  (interceptor
   {:name ::upper
    :enter (fn [{:keys [request] :as context}]
             (let [x (-> request
                         :path-params
                         :x)
                   resp {:status 200
                         :body {:x x
                                :x2 (clojure.string/upper-case x)}
                         :headers {}}]
               (assoc context :response resp)))
    }))

(def compo
  (interceptor
   {:name ::compo
    :enter (fn [{:keys [request] :as context}]
             (let [resp {:status 200
                         :body {:component
                                (p.p/use-component request)}
                         :headers {}}]
               (assoc context :response resp)))
    }))

(def api-routes
  [["/api/json" :get [http/json-body inject-response-with-session json-example]]
   ["/api/transit" :get [http/transit-body inject-response-with-session transit-example]]
   ["/api/pemangkat/:x" :get [http/json-body inject-response-with-session pemangkat]]
   ["/api/uppercase/*x" :get [http/json-body inject-response-with-session upper]]
   ["/api/component" 
    :get
    [http/json-body
     inject-response-with-session
     (p.p/using-component :mycomponent)
     compo]]]
  )

(defn make-api-routes
  [config]
  (->> api-routes
       (mapv (fn [r]
              (update r 2 #(vec (concat middlewares %)))))))
