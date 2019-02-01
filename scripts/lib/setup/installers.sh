#!/usr/bin/env bash

_localPropertiesPath=./android/local.properties

function downloadUrl() {
  if program_exists "aria2c"; then
    aria2c --max-connection-per-server=16 --split=16 --dir="$1" -o "$2" "$3"
  else
    wget --show-progress --output-document="$1/$2" "$3"
  fi
}

function install_nsis() {
  # NSIS (Nullsoft Scriptable Install System) is a professional open source system to create Windows installers. It is designed to be as small and flexible as possible and is therefore very suitable for internet distribution.
  linux_install nsis
}

function export_android_sdk_vars() {
  local profile
  local target_path
  if is_macos; then
    profile=$HOME/.bash_profile
  elif is_linux; then
    profile=$HOME/.bashrc
  fi

  [ -f $profile ] || touch $profile
  if ! grep -Fq "export ANDROID_SDK_ROOT=" $profile; then
    echo "export ANDROID_HOME=\"$1\"" >> $profile && \
    echo "export ANDROID_SDK_ROOT=\"$1\"" >> $profile && \
    echo "export PATH=\"$1/tools:$1/tools/bin:\$PATH\"" >> $profile
  fi
  export ANDROID_HOME="$1" && \
  export ANDROID_SDK_ROOT="$1" && \
  export PATH="$1/tools:$1/tools/bin:$PATH"
}

function install_android_sdk() {
  if [ -z "$ANDROID_SDK_ROOT" ]; then
    if grep -Fq "sdk.dir" $_localPropertiesPath; then
      local _sdkParentDir="$(awk -F'=' "/^sdk.dir=/{print \$2}" "$_localPropertiesPath")"
      export_android_sdk_vars $_sdkParentDir
      cecho "@green[[Android SDK already declared.]]"
    else
      local required_version=$(toolversion android-sdk)
      local _sdkParentDir=$HOME/Android/Sdk
      mkdir -p $_sdkParentDir
      cecho "@cyan[[Downloading Android SDK.]]"

      local osname=$(echo "$OS" | tr '[:upper:]' '[:lower:]')
      downloadUrl . sdk-tools-${osname}.zip https://dl.google.com/android/repository/sdk-tools-${osname}-${required_version}.zip && \
        cecho "@cyan[[Extracting Android SDK to $_sdkParentDir.]]" && \
        unzip -q -o ./sdk-tools-${osname}.zip -d "$_sdkParentDir" && \
        rm -f ./sdk-tools-${osname}.zip && \
        _sdkTargetDir="$_sdkParentDir" && \
        echo "sdk.dir=$_sdkTargetDir" | tee -a $_localPropertiesPath && \
        export_android_sdk_vars $_sdkParentDir && \
        cecho "@blue[[Android SDK installation completed in $_sdkTargetDir.]]" || \
        return 0
    fi
  else
    if ! grep -Fq "sdk.dir" $_localPropertiesPath; then
      echo "sdk.dir=$ANDROID_SDK_ROOT" | tee -a $_localPropertiesPath
    fi
    cecho "@green[[Android SDK already declared.]]"
  fi

  use_android_sdk

  return 1
}

function install_react_native_cli() {
  cd "$(repo_path)"

  local npm_command='npm'
  local required_version=$(toolversion react_native_cli)

  if is_linux; then
    # aptitude version of node requires sudo for global install
    npm_command="sudo $npm_command"
  fi

  if npm list "react-native-cli@{required_version}" &>/dev/null; then
    already_installed "react-native-cli@{required_version}"
  else
    $npm_command install --no-save react-native-cli@${required_version}
  fi
}

function required_pod_version() {
  cat "$(repo_path)/ios/Podfile.lock" | grep "COCOAPODS: " | awk '{ print $2 }'
}

function correct_pod_version_is_installed() {
  ! program_exists "pod" && return 1

  [[ "$(required_pod_version)" == "$(pod --version)" ]]
}

function using_rvm() {
  program_exists "rvm"
}

function initialize_rvm() {
  cd "$(repo_path)"

  if [ ! -e "$(repo_path)/.ruby-version" ]; then
    rvm use --default > /dev/null
    echo "$(rvm current)" > .ruby-version
  fi

  rvm use . >/dev/null
}

function using_cocoapods() {
  is_macos
}

function install_cocoapods() {
  ! using_cocoapods && return 1

  local gem_command="sudo gem"
  local destination="system Ruby"
  local version=$(required_pod_version)

  if using_rvm; then
    initialize_rvm

    gem_command="gem"
    destination="RVM ($(rvm current))"
  fi

  if ! program_exists "pod"; then
    $gem_command install cocoapods -v "$version"
  elif ! correct_pod_version_is_installed; then
    cecho "@b@blue[[+ Updating to cocoapods $version]]"

    $gem_command uninstall cocoapods --ignore-dependencies --silent
    $gem_command install cocoapods -v "$version"
  else
    cecho "+ cocoapods already installed to $destination... skipping."
  fi
}

function dependency_setup() {
  cecho "@b@blue[[\$ $@]]"
  echo

  cd "$(repo_path)"
  eval "$@" || (cecho "@b@red[[Error running dependency install '$@']]" && exit 1)

  echo
  echo "  + done"
  echo
}

function use_android_sdk() {
  if [ -n "$ANDROID_SDK_ROOT" ]; then
    if ! grep -Fq "sdk.dir" $_localPropertiesPath; then
      echo "sdk.dir=$ANDROID_SDK_ROOT" | tee -a $_localPropertiesPath
    fi

    local ANDROID_BUILD_TOOLS_VERSION=$(toolversion android-sdk-build-tools)
    local ANDROID_PLATFORM_VERSION=$(toolversion android-sdk-platform)
    touch ~/.android/repositories.cfg
    echo y | sdkmanager "platform-tools" "build-tools;$ANDROID_BUILD_TOOLS_VERSION" "platforms;$ANDROID_PLATFORM_VERSION"
    yes | sdkmanager --licenses
  else
    local _docUrl="https://status.im/build_status/"
    cecho "@yellow[[ANDROID_SDK_ROOT environment variable not defined, please install the Android SDK.]]"
    cecho "@yellow[[(see $_docUrl).]]"

    echo

    exit 1
  fi

  scripts/generate-keystore.sh
}

function install_android_ndk() {
  if grep -Fq "ndk.dir" $_localPropertiesPath; then
    cecho "@green[[Android NDK already declared.]]"
  else
    local ANDROID_NDK_VERSION=$(toolversion android-ndk)
    local _ndkParentDir=~/Android/Sdk
    mkdir -p $_ndkParentDir
    cecho "@cyan[[Downloading Android NDK.]]"

    local PLATFORM="linux"
    if is_macos; then
        PLATFORM="darwin"
    fi

    downloadUrl . android-ndk.zip https://dl.google.com/android/repository/android-ndk-$ANDROID_NDK_VERSION-$PLATFORM-x86_64.zip && \
      cecho "@cyan[[Extracting Android NDK to $_ndkParentDir.]]" && \
      unzip -q -o ./android-ndk.zip -d "$_ndkParentDir" && \
      rm -f ./android-ndk.zip && \
      _ndkTargetDir="$_ndkParentDir/$(ls $_ndkParentDir | grep ndk)" && \
      echo "ndk.dir=$_ndkTargetDir" | tee -a $_localPropertiesPath && \
      cecho "@blue[[Android NDK installation completed in $_ndkTargetDir.]]"
  fi
}
