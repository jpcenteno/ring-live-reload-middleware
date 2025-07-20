#! /bin/sh
set -eu

clj -M:build-cljs watch client "${@}"
