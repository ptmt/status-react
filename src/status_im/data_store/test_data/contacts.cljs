(ns status-im.data-store.test-data.contacts
  (:require [status-im.data-store.realm.contacts :as data-store]
            [taoensso.timbre :as log]))



(def contacts
  [{:address          "test_address_1"
    :whisper-identity "test_identity_1"
    :name             "Allen Poe"}
   {:address          "test_address_2"
    :whisper-identity "test_identity_2"
    :name             "John Doe"}
   {:address          "test_address_3"
    :whisper-identity "test_identity_3"
    :name             "Hara Kiri"}
   {:address          "test_address_4"
    :whisper-identity "test_identity_4"
    :name             "Zeus Greek"}
   {:address          "test_address_5"
    :whisper-identity "test_identity_5"
    :name             "Simon Machiavelli"}
   {:address          "test_address_6"
    :whisper-identity "test_identity_6"
    :name             "Andrew Bennet"}
   {:address          "test_address_7"
    :whisper-identity "test_identity_7"
    :name             "Some Name"}
   {:address          "test_address_8"
    :whisper-identity "test_identity_8"
    :name             "Jason Bourne"}
   {:address          "test_address_9"
    :whisper-identity "test_identity_9"
    :name             "Johny Cash"}])

(defn add-test-contacts
  []
  (log/debug "Contacts added already ?"(data-store/exists? "test_identity_1"))
  (when-not (data-store/exists? "test_identity_1")
    (doseq [contact contacts]
      (log/debug "Adding contact " contact)
      (data-store/save contact false))))