(ns ^:figwheel-no-load env.android.main
  (:require [om.next :as om]
            [messenger.android.core :as core]
            [messenger.omnext :as omnext]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :heads-up-display true
  :jsload-callback #(om/add-root! omnext/reconciler core/AppRoot 1))

(core/init)

(def root-el (core/app-root))