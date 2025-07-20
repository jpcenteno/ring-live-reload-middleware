#! /bin/sh
set -eu

clj -M:build-cljs build client "${@}"
