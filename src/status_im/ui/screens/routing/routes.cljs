(ns status-im.ui.screens.routing.routes
  (:require
   [status-im.ui.screens.main-tabs.views :as main-tabs]

   [status-im.ui.screens.accounts.login.views :refer [login]]
   [status-im.ui.screens.accounts.recover.views :refer [recover]]
   [status-im.ui.screens.accounts.views :refer [accounts]]

   [status-im.ui.screens.progress.views :refer [progress]]

   [status-im.ui.screens.chat.views :refer [chat chat-modal]]
   [status-im.ui.screens.add-new.views :refer [add-new]]
   [status-im.ui.screens.add-new.new-chat.views :refer [new-chat]]
   [status-im.ui.screens.add-new.new-public-chat.view :refer [new-public-chat]]

   [status-im.ui.screens.qr-scanner.views :refer [qr-scanner]]

   [status-im.ui.screens.group.views :refer [new-group
                                             contact-toggle-list
                                             add-participants-toggle-list]]
   [status-im.ui.screens.profile.user.views :as profile.user]
   [status-im.ui.screens.profile.contact.views :as profile.contact]
   [status-im.ui.screens.profile.group-chat.views :as profile.group-chat]
   [status-im.ui.screens.profile.photo-capture.views :refer [profile-photo-capture]]
   [status-im.extensions.views :refer [take-picture]]
   [status-im.ui.screens.wallet.main.views :as wallet.main]
   [status-im.ui.screens.wallet.collectibles.views :refer [collectibles-list]]
   [status-im.ui.screens.wallet.send.views :refer [send-transaction send-transaction-modal sign-message-modal]]
   [status-im.ui.screens.wallet.choose-recipient.views :refer [choose-recipient]]
   [status-im.ui.screens.wallet.request.views :refer [request-transaction send-transaction-request]]
   [status-im.ui.screens.wallet.components.views :as wallet.components]
   [status-im.ui.screens.wallet.onboarding.views :as wallet.onboarding]
   [status-im.ui.screens.wallet.transaction-fee.views :as wallet.transaction-fee]
   [status-im.ui.screens.wallet.settings.views :as wallet-settings]
   [status-im.ui.screens.wallet.transactions.views :as wallet-transactions]
   [status-im.ui.screens.wallet.transaction-sent.views :refer [transaction-sent transaction-sent-modal]]
   [status-im.ui.screens.wallet.components.views :refer [contact-code recent-recipients recipient-qr-code]]
   [status-im.ui.screens.contacts-list.views :refer [contacts-list blocked-users-list]]
   [status-im.ui.screens.network-settings.views :refer [network-settings]]
   [status-im.ui.screens.network-settings.network-details.views :refer [network-details]]
   [status-im.ui.screens.network-settings.edit-network.views :refer [edit-network]]
   [status-im.ui.screens.extensions.views :refer [extensions-settings selection-modal-screen]]
   [status-im.ui.screens.log-level-settings.views :refer [log-level-settings]]
   [status-im.ui.screens.fleet-settings.views :refer [fleet-settings]]
   [status-im.ui.screens.offline-messaging-settings.views :refer [offline-messaging-settings]]
   [status-im.ui.screens.offline-messaging-settings.edit-mailserver.views :refer [edit-mailserver]]
   [status-im.ui.screens.extensions.add.views :refer [edit-extension show-extension show-extension-modal]]
   [status-im.ui.screens.bootnodes-settings.views :refer [bootnodes-settings]]
   [status-im.ui.screens.pairing.views :refer [installations]]
   [status-im.ui.screens.bootnodes-settings.edit-bootnode.views :refer [edit-bootnode]]
   [status-im.ui.screens.currency-settings.views :refer [currency-settings]]
   [status-im.ui.screens.hardwallet.settings.views :refer [keycard-settings reset-card]]
   [status-im.ui.screens.help-center.views :refer [help-center]]
   [status-im.ui.screens.browser.views :refer [browser]]
   [status-im.ui.screens.add-new.open-dapp.views :refer [open-dapp dapp-description]]
   [status-im.ui.screens.intro.views :refer [intro]]
   [status-im.ui.screens.accounts.create.views :refer [create-account]]
   [status-im.ui.screens.hardwallet.authentication-method.views :refer [hardwallet-authentication-method]]
   [status-im.ui.screens.hardwallet.connect.views :refer [hardwallet-connect]]
   [status-im.ui.screens.hardwallet.pin.views :refer [enter-pin]]
   [status-im.ui.screens.hardwallet.setup.views :refer [hardwallet-setup]]
   [status-im.ui.screens.hardwallet.success.views :refer [hardwallet-success]]
   [status-im.ui.screens.profile.seed.views :refer [backup-seed]]
   [status-im.ui.screens.about-app.views :as about-app]
   [status-im.ui.screens.stickers.views :as stickers]
   [status-im.ui.screens.dapps-permissions.views :as dapps-permissions]))

(def all-routes
  {:login                            login
   :progress                         progress
   :create-account                   create-account
   :recover                          recover
   :accounts                         accounts
   :intro                            intro
   :hardwallet-authentication-method hardwallet-authentication-method
   :hardwallet-connect               hardwallet-connect
   :enter-pin                        enter-pin
   :hardwallet-setup                 hardwallet-setup
   :hardwallet-success               hardwallet-success
   :home                             (main-tabs/get-main-tab :home)
   :chat                             chat
   :profile                          profile.contact/profile
   :new                              add-new
   :new-chat                         new-chat
   :qr-scanner                       qr-scanner
   :profile-qr-viewer                [:modal profile.user/qr-viewer]
   :take-picture                     take-picture
   :new-group                        new-group
   :add-participants-toggle-list     add-participants-toggle-list
   :contact-toggle-list              contact-toggle-list
   :group-chat-profile               profile.group-chat/group-chat-profile
   :new-public-chat                  new-public-chat
   :open-dapp                        open-dapp
   :dapp-description                 dapp-description
   :browser                          browser
   :stickers                         stickers/packs
   :stickers-pack                    stickers/pack
   :wallet-modal                     [:modal wallet.main/wallet-modal]
   :chat-modal                       [:modal chat-modal]
   :show-extension-modal             [:modal show-extension-modal]
   :wallet-send-transaction-modal    [:modal send-transaction-modal]
   ;; TODO check non uniq name :wallet-transaction-sent
   :wallet-transaction-sent-modal    [:modal transaction-sent-modal]
   :wallet-transaction-fee           [:modal wallet.transaction-fee/transaction-fee]
   :wallet-onboarding-setup-modal    [:modal wallet.onboarding/modal]
   :wallet-sign-message-modal        [:modal sign-message-modal]
   :wallet                           (main-tabs/get-main-tab :wallet)
   :collectibles-list                collectibles-list
   :wallet-onboarding-setup          wallet.onboarding/screen
   :wallet-send-transaction-chat     send-transaction
   :contact-code                     contact-code
   :wallet-send-transaction          send-transaction
   :recent-recipients                recent-recipients
   :wallet-transaction-sent          transaction-sent
   :recipient-qr-code                recipient-qr-code
   :wallet-send-assets               wallet.components/send-assets
   :wallet-request-transaction       request-transaction
   :wallet-send-transaction-request  send-transaction-request
   :wallet-request-assets            wallet.components/request-assets
   :unsigned-transactions            wallet-transactions/transactions
   :transactions-history             wallet-transactions/transactions
   :wallet-transaction-details       wallet-transactions/transaction-details
   :wallet-settings-hook             wallet-settings/settings-hook
   :selection-modal-screen           [:modal selection-modal-screen]
   :wallet-settings-assets           [:modal wallet-settings/manage-assets]
   :wallet-transactions-filter       [:modal wallet-transactions/filter-history]
   ;;profile
   :my-profile                       (main-tabs/get-main-tab :my-profile)
   :contacts-list                    contacts-list
   :blocked-users-list               blocked-users-list
   :profile-photo-capture            profile-photo-capture
   :about-app                        about-app/about-app
   :bootnodes-settings               bootnodes-settings
   :installations                    installations
   :edit-bootnode                    edit-bootnode
   :offline-messaging-settings       offline-messaging-settings
   :edit-mailserver                  edit-mailserver
   :help-center                      help-center
   :dapps-permissions                dapps-permissions/dapps-permissions
   :manage-dapps-permissions         dapps-permissions/manage
   :extensions-settings              extensions-settings
   :edit-extension                   edit-extension
   :show-extension                   show-extension
   :network-settings                 network-settings
   :network-details                  network-details
   :edit-network                     edit-network
   :log-level-settings               log-level-settings
   :fleet-settings                   fleet-settings
   :currency-settings                currency-settings
   :backup-seed                      backup-seed
   :reset-card                       reset-card
   :keycard-settings                 keycard-settings})
