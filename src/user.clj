;; Copyright © 2016-2019, JUXT LTD.

(ns user
  (:require
   [clojure.tools.namespace.repl :refer :all]
   [clojure.java.classpath :refer [classpath-directories]]
   [io.aviso.ansi]
   [integrant.repl.state]
   [spyscope.core]))

;; Work around TNS-45.  This used to be fixed by using a forked version of tns,
;; but because it now comes in transitively, it cannot be compared.  This might
;; be fixed by TDEPS-17.
(let [edge-target? (fn [f]
                     ;; match target, target/dev target/prod, etc.
                     (re-matches #".*target(/\w+)?" (str f)))]
  (apply set-refresh-dirs
         (remove edge-target? (classpath-directories))))

(let [prefix "edge.load_"]
  (doseq [[prop _]
          (filter
            (fn [[prop _]]
              (.startsWith prop prefix))
            (into {} (System/getProperties)))]
    (require (symbol (subs prop (count prefix))))))

(let [lock (Object.)]
  (defn dev
    "Call this to switch to the dev namespace."
    []
    (println "[Ātea] Chargement du code Clojure, veuillez patienter...")
    (locking lock
      (require 'dev))
    (when-not integrant.repl.state/system
      (println (io.aviso.ansi/bold-blue "[Ātea] Entrez (go) pour démarrer le système de développement")))
    (in-ns 'dev)))

(defn fixed!
  "If, for some reason, the Clojure code in the project fails to
  compile - we still bring up a REPL to help debug the problem. Once
  the problem has been resolved you can call this function to continue
  development."
  []
  (refresh-all)
  (in-ns 'dev))
