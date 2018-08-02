#!/usr/bin/env bash

SBT_OPTS="-XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=2G -Xmx2G"
sbt test
