#!/bin/bash

cd ~/var/bartera-dev
kill `less RUNNING_PID`
git checkout dev
git pull
play -Dconfig.resource=dev.conf -Dhttp.port=9090 clean compile start
