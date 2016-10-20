(ns status-im.chat.handlers.webview-bridge
  (:require [re-frame.core :refer [after dispatch enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.utils.handlers :as u]
            [status-im.utils.types :as t]
            [status-im.i18n :refer [label]]
            [status-im.contacts.views.contact-list :refer [contact-list]]
            [status-im.qr-scanner.screen :refer [qr-scanner]]
            [status-im.accounts.views.wallet-qr-code :refer [wallet-qr-code]]
            [taoensso.timbre :as log]))

(register-handler :set-webview-bridge
  (fn [db [_ bridge]]
    (assoc db :webview-bridge bridge)))

(defn contacts-click-handler [whisper-identity action]
  (log/debug "Contact clicked: " whisper-identity action)
  (dispatch [:set :chat-modal nil])
  (if (= whisper-identity :qr-scan)
    (if (= action :send)
      (dispatch [:send-to-webview-bridge {:data "test"
                                          :event (name :webview-send-transaction)}])
      (dispatch [:show-qr-code]))
    (if (= action :send)
      (dispatch [:chat-with-command whisper-identity :send])
      (dispatch [:chat-with-command whisper-identity :receive]))))

(defn scan-qr-handler
  [data]
  (log/debug "scaned qr" data))

(defn chat-with-command
  [_ [_ whisper-identity command]]
  (dispatch [:start-chat whisper-identity {}])
  (dispatch [:remove-contacts-click-handler])
  (let [callback #(dispatch [:set-chat-command command])]
    (dispatch [:add-commands-loading-callback whisper-identity callback])))

(register-handler :chat-with-command
  (u/side-effect! chat-with-command))

(register-handler :webview-bridge-message
  (u/side-effect!
    (fn [_ [_ message-string]]
      (let [message (t/json->clj message-string)
            event   (keyword (:event message))]
        (log/debug (str "message from webview: " message))
        (case event
          :webview-send-transaction (dispatch [:show-contacts-menu contacts-click-handler :send])
          :webview-receive-transaction (dispatch [:show-contacts-menu contacts-click-handler :receive])
          :webview-scan-qr (dispatch [:show-scan-qr scan-qr-handler])
          (log/error (str "Unknown event: " event)))))))

(register-handler :show-contacts-menu
  (fn [db [_ click-handler action]]
    (assoc db :contacts-click-handler click-handler
              :contacts-click-action  action
              :chat-modal contact-list)))

(register-handler :show-scan-qr
  (fn [db [_ click-handler]]
    (-> db
        (assoc-in [:qr-codes {:toolbar-title (label :t/address)}] click-handler)
        (assoc :chat-modal qr-scanner))))

(register-handler :show-qr-code
  (fn [db [_]]
    (-> db
        (assoc :chat-modal wallet-qr-code))))


(register-handler :send-to-webview-bridge
  (u/side-effect!
    (fn [{:keys [webview-bridge]} [_ data]]
      (when webview-bridge
        (.sendToBridge webview-bridge (t/clj->json data))))))
