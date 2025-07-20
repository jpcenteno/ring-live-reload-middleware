#! /bin/sh
set -eu

clj -M:build-cljs release client "${@}"
