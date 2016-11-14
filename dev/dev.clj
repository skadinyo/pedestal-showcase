(ns dev
  (:require 
   [com.stuartsierra.component :as component]
   [clojure.tools.namespace.repl :as ns-repl]
   [clojure.edn :as edn]
   [pedestal-showcase.system :as system]))

(def dev-system nil)

(defn dev-config
  "return development configuration map"
  []
  {})


(defn init []
  (alter-var-root #'dev-system
                  (constantly (system/init-dev-system (dev-config)))))


(defn start []
  (alter-var-root #'dev-system component/start))


(defn stop []
  (alter-var-root #'dev-system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))


(defn reset []
  (stop)
  (ns-repl/refresh :after 'dev/go))


#_ (ns-repl/refresh-all)
