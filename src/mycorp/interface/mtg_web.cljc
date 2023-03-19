(ns mycorp.interface.mtg-web
    (:require
     #?(:clj [datascript.core :as d])   ; database on server
     [hyperfiddle.electric :as e]
     [hyperfiddle.electric-dom2 :as dom]
     [hyperfiddle.electric-ui4 :as ui]
     [shadow.css :refer [css]]))

(defonce !conn #?(:clj (d/create-conn {}) :cljs nil)) ; database on server
(e/def db) ; injected database ref; Electric defs are always dynamic

(e/defn Root []
  (e/server
    (binding [db (e/watch !conn)]
      (e/client
        (dom/link (dom/props {:rel :stylesheet :href "/css/ui.css"}))
        (dom/h1 (dom/props {:class (css :bg-lime-800)})
          (dom/text "minimal todo list"))))))
