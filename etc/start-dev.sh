#!/bin/bash

cd ~/var/bartera-dev
git checkout dev
git pull
play start -Dconfig.resource=dev.conf -Dhttp.port=9090
