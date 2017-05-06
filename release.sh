#!/usr/bin/env bash

set -e

read -p "Are you sure you want to release? It's permanent! (press any key to continue)"
mvn release:clean release:prepare

read -p "Are you still sure you want to release? It's permanent! (press any key to continue)"
mvn release:perform