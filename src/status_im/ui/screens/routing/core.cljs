(ns status-im.ui.screens.routing.core
  (:require
   [re-frame.core :refer [dispatch]]
   [status-im.utils.platform :refer [android?]]
   [status-im.ui.components.react :refer [view modal create-main-screen-view] :as react]
   [status-im.ui.components.styles :as common-styles]
   [status-im.utils.navigation :as navigation]
   [cljs-react-navigation.reagent :as nav-reagent]
   [re-frame.core :as re-frame]
   [taoensso.timbre :as log]
   [status-im.utils.platform :as platform]
   [status-im.utils.core :as utils]
   [status-im.ui.screens.routing.routes :as routes]
   [status-im.ui.screens.routing.intro-login-stack :as intro-login-stack]
   [status-im.ui.screens.routing.chat-stack :as chat-stack]
   [status-im.ui.screens.routing.wallet-stack :as wallet-stack]
   [status-im.ui.screens.routing.profile-stack :as profile-stack]
   [status-im.ui.screens.main-tabs.views :as tabs]))

(defn wrap [view-id component]
  (fn []
    (let [main-view (create-main-screen-view view-id)]
      [main-view common-styles/flex
       [component]
       [:> navigation/navigation-events
        {:on-will-focus
         (fn []
           (log/debug :on-will-focus view-id)
           (re-frame/dispatch [:screens/on-will-focus view-id]))}]])))

(defn wrap-modal [modal-view component]
  (fn []
    (if platform/android?
      [view common-styles/modal
       [modal {:transparent      true
               :animation-type   :slide
               :on-request-close (fn []
                                   (cond
                                     (#{:wallet-send-transaction-modal
                                        :wallet-sign-message-modal}
                                      modal-view)
                                     (dispatch [:wallet/discard-transaction-navigate-back])

                                     :else
                                     (dispatch [:navigate-back])))}
        [react/main-screen-modal-view modal-view
         [component]]]]
      [react/main-screen-modal-view modal-view
       [component]])))

(defn get-screen [k]
  (get routes/all-routes k))

(declare stack-screens)

(defn prepare-config [config]
  (-> config
      (utils/update-if-present :initialRouteName name)
      (utils/update-if-present :mode name)))

(defn stack-navigator [routes config]
  (nav-reagent/stack-navigator
   routes
   (merge {:headerMode "none"} (prepare-config config))))

(defn switch-navigator [routes config]
  (nav-reagent/switch-navigator
   routes
   (prepare-config config)))

(defn tab-navigator [routes config]
  (nav-reagent/tab-navigator
   routes
   (prepare-config config)))

(defn build-screen [screen]
  (let [[screen-name screen-config]
        (cond (keyword? screen)
              [screen (get-screen screen)]
              (map? screen)
              [(:name screen) screen]
              :else screen)]
    (let [res (cond
                (map? screen-config)
                (let [{:keys [screens config]} screen-config]
                  (stack-navigator
                   (stack-screens screens)
                   config))

                (vector? screen-config)
                (let [[_ screen] screen-config]
                  (nav-reagent/stack-screen
                   (wrap-modal screen-name screen)))

                :else
                (nav-reagent/stack-screen (wrap screen-name screen-config)))]
      [screen-name (cond-> {:screen res}
                     (:navigation screen-config)
                     (assoc :navigationOptions
                            (:navigation screen-config)))])))

(defn stack-screens [screens-map]
  (->> screens-map
       (map build-screen)
       (into {})))

(defn get-main-component [view-id]
  (log/debug :component view-id)
  (switch-navigator
   (into {}
         [(build-screen (intro-login-stack/intro-login-stack view-id))
          [:tabs
           {:screen (tab-navigator
                     (->> [(build-screen chat-stack/chat-stack)
                           (build-screen wallet-stack/wallet-stack)
                           (build-screen profile-stack/profile-stack)]
                          (into {}))
                     {:initialRouteName :chat-stack
                      :tabBarComponent  (reagent.core/reactify-component
                                         (fn [args]
                                           (let [idx (.. (:navigation
                                                          args)
                                                         -state
                                                         -index)

                                                 tab   (case idx
                                                         0 :chat-stack
                                                         1 :wallet-stack
                                                         2 :home-stack
                                                         :chat-stack)]
                                             [tabs/tabs tab])))})}]])
   {:initialRouteName :intro-login-stack}))
