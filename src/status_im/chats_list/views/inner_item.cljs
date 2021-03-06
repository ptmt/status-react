(ns status-im.chats-list.views.inner-item
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [status-im.components.react :refer [view image icon text]]
            [status-im.components.chat-icon.screen :refer [chat-icon-view-chat-list]]
            [status-im.models.commands :refer [parse-command-message-content]]
            [status-im.chats-list.styles :as st]
            [status-im.utils.utils :refer [truncate-str]]
            [status-im.i18n :refer [label label-pluralize]]
            [status-im.utils.datetime :as time]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.constants :refer [console-chat-id
                                         content-type-command
                                         content-type-command-request]]
            [taoensso.timbre :as log]))

(defmulti message-content (fn [{:keys [content-type] :as message}] content-type))

(defmethod message-content content-type-command
  [{{:keys [command params]} :content}]
  (let [kw (keyword (str "t/command-text-" (name command)))]
    (label kw params)))

(defmethod message-content content-type-command-request
  [{{:keys [content]} :content}]
  content)

(defmethod message-content :default
  [{:keys [content]}]
  content)

(defn message-content-text [message]
  (let [content (message-content message)]
    (if (str/blank? content)
      [text {:style st/last-message-text-no-messages}
       (label :t/no-messages)]
      [text {:style           st/last-message-text
             :number-of-lines 2}
       content])))

(defview message-status [{:keys [chat-id contacts]}
                         {:keys [message-id message-status user-statuses message-type outgoing] :as msg}]
  [app-db-message-status-value [:get-in [:message-statuses message-id :status]]]
  (let [delivery-status (get-in user-statuses [chat-id :status])]
    (when (and outgoing
               (or (some #(= (keyword %) :seen) [delivery-status
                                                 message-status
                                                 app-db-message-status-value])
                   (and (= (keyword message-type) :group-user-message)
                        (and (= (count user-statuses) (count contacts))
                             (every? (fn [[_ {:keys [status]}]]
                                       (= (keyword status) :seen)) user-statuses)))
                   (= chat-id console-chat-id)))
      [image {:source {:uri :icon_ok_small}
              :style  st/status-image}])))

(defn message-timestamp [{:keys [timestamp]}]
  (when timestamp
    [text {:style st/datetime-text}
     (time/to-short-str timestamp)]))

(defview unviewed-indicator [chat-id]
  [unviewed-messages [:unviewed-messages-count chat-id]]
  (when (pos? unviewed-messages)
    [view st/new-messages-container
     [text {:style st/new-messages-text
            :font  :medium}
      unviewed-messages]]))

(defn chat-list-item-inner-view [{:keys [chat-id name color last-message
                                         online group-chat contacts] :as chat}]
  (let [last-message (or (first (sort-by :clock-value > (:messages chat)))
                         last-message)
        name         (or name (generate-gfy))]
    [view st/chat-container
     [view st/chat-icon-container
      [chat-icon-view-chat-list chat-id group-chat name color online]]
     [view st/item-container
      [view st/name-view
       [text {:style st/name-text
              :font  :medium}
        (if (str/blank? name)
          (generate-gfy)
          (truncate-str name 30))]
       (when group-chat
         [icon :group st/group-icon])
       (when group-chat
         [text {:style st/memebers-text}
          (label-pluralize (inc (count contacts)) :t/members)])]
      [message-content-text last-message]]
     [view
      (when last-message
        [view st/status-container
         [message-status chat last-message]
         [message-timestamp last-message]])
      [unviewed-indicator chat-id]]]))
