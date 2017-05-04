#!/bin/bash

set -ev

case `uname` in
    Darwin)     OS_NAME="osx"    ;;
    Linux)      OS_NAME="linux"  ;;
esac

JDK_VERSION_LEVEL=${JDK_VERSION:(-1)}
case ${OS_NAME} in
    osx)
        if [[ ${JDK_VERSION} == openjdk* ]]; then
            echo "OpenJDK is not supported on OSX, defaulting to oraclejdk${JDK_VERSION_LEVEL}"
            export JDK_VERSION=oraclejdk${JDK_VERSION_LEVEL}
        fi

        # Install the correct version of Java:

        case ${JDK_VERSION_LEVEL} in
            8)
                CASK_PKG=caskroom/cask;
                JAVA_PKG="java"
                ;;
            7|9)
                CASK_PKG=caskroom/versions;
                JAVA_PKG="java${JDK_VERSION_LEVEL}";
                ;;
        esac

        echo "Installing ${JDK_VERSION} from: ${CASK_PKG}:${JAVA_PKG}"

        brew tap ${CASK_PKG}

        echo "brew update ..." ; brew update > /dev/null #; brew doctor; brew update

        brew cask install ${JAVA_PKG}
      ;;

    linux)
        # Required for testing JavaFX tests via Monocle
        export DISPLAY=:99.0
        sh -e /etc/init.d/xvfb start
      ;;
esac