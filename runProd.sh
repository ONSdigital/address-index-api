#!/usr/bin/env bash

JAVA_OPTS="-DONS_AI_LIBRARY_PATH=../../../../parsers/src/main/resources" sbt "project address-index-server" "runProd 9000"

