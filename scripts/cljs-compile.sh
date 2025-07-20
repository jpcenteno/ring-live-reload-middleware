#! /bin/sh
set -eu

clj -M:build-cljs compile client "${@}"
