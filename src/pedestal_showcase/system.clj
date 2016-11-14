(ns pedestal-showcase.system
  (:require
   [com.stuartsierra.component :as component]
   [pedestal-showcase.pedestal :as pedestal]
   [io.pedestal.http :as http]
   [io.pedestal.interceptor :refer [interceptor]]
   [io.pedestal.log :as log]
   [io.pedestal.http.csrf :as csrf]
   [io.pedestal.http.body-params :as body-params]
   [io.pedestal.http.route.definition.table :as table]
   [io.pedestal.interceptor :refer [interceptor interceptor-name]]
   [io.pedestal.http.ring-middlewares :as p.rm]
   [pedestal-showcase.routes.home :as home]
   [pedestal-showcase.routes.api :as api]
   ))

(def common-interceptors
  [http/html-body
   (body-params/body-params)
   (p.rm/multipart-params)
   #_(csrf/anti-forgery)])


(defn inject-common-interceptors
  "Injecting middleware in each route"
  [plain-routes common-interceptors]
  (->> plain-routes
       (mapv (fn [route]
               (update-in route [2]
                          (fn [it]
                            (into common-interceptors it)))))))


(defn plain-routes
  "Combining home and api routes"
  [config]
  (->> (home/make-home-routes config)
       (into (api/make-api-routes config))))


(defn routes-with-common-interceptors
  [config]
  (inject-common-interceptors
   (plain-routes config)
   common-interceptors))


(defn app-routes
  "Creating the actual routes"
  [config]
  (table/table-routes
   {}
   (routes-with-common-interceptors config)
   ))


(defn pedestal-config
  [config]
  {::http/host "0.0.0.0"
   ::http/port 5050
   ::http/type :jetty
   ::http/join? false
   ::http/resource-path "/public"
   ::http/enable-session {} 
   ;;::http/enable-csrf {}
   ::http/routes (app-routes config)})


(defn pedestal-config-fn
  ""
  [config]
  (http/default-interceptors (pedestal-config config)))

(defn init-production-system
  ""
  [config]
  (component/system-map
   :mycomponent {:my :component}
   :pedestal (component/using
              (pedestal/pedestal pedestal-config-fn config)
              [:mycomponent])))

(defn init-dev-system
  ""
  [config]
  (merge
   (init-production-system config)
   (component/system-map
    :mycomponent {:my :component}
    :pedestal (component/using
               (pedestal/dev-pedestal pedestal-config-fn config)
               [:mycomponent]))))


