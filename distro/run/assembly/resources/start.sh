#!/bin/bash

BASEDIR=$(dirname "$0")
runScript=$BASEDIR/internal/run.sh

if [ $# -eq 0 ]; then
  # start Camunda Run in the background
  exec $runScript start --detached
else
  # start Camunda Run with the passed arguments
  exec $runScript start "$@"
fi

