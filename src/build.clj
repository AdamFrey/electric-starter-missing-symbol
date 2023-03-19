(ns build
  (:require
   [shadow.cljs.devtools.server.fs-watch :as fs-watch]
   [shadow.css.build :as cb]
   [clojure.java.io :as io]))

(defonce *css (atom nil))
(defonce *css-watch (atom nil))

(defn css-initial-state []
  (-> (cb/start)
      (cb/index-path (io/file "src" "mycorp") {})))

(defn log-warnings! [build-state]
  (doseq [mod                                (:outputs build-state)
          {:keys [warning-type] :as warning} (:warnings mod)]

    (prn [:CSS (name warning-type) (dissoc warning :warning-type)])))

(def css-out-dir (io/file "resources" "public" "css"))
(def css-gen-config
  '{:ui
    {:include
     [mycorp*]}})

(defn write-css! [build-state]
  (println "Writing css...")
  (-> build-state
      (cb/generate css-gen-config)
      (cb/write-outputs-to css-out-dir)
      (doto log-warnings!))
  ::css-write-done)

(defn release-css! []
  (write-css! (css-initial-state)))

(comment
  (release-css!)
  )

(defn start-css-build!
  {:shadow/requires-server true}
  []
  (reset! *css (css-initial-state))
  (write-css! @*css)
  (reset! *css-watch
    (fs-watch/start
      {}
      [(io/file "src" "mycorp")]
      ["cljs" "cljc" "clj"]
      (fn [updates]
        (try
          (doseq [{:keys [file event]} updates
                  :when                (not= event :del)]
            ;; re-index all added or modified files
            (swap! *css cb/index-file file))
          (write-css! @*css)
          (catch Exception e
            (prn :css-build-failure)
            (prn e)))))))

(defn stop-css-build! []
  (when-some [css-watch @*css-watch]
    (fs-watch/stop css-watch)
    (reset! *css nil))
  ::css-build-stopped)

(comment
  (start-css-build!)
  (stop-css-build!)
  ;; (release-css!)
  )
