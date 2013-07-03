#!/bin/bash

VERSION=0.1.0-SNAPSHOT
DIR=bartera-$VERSION
CONF=~/code/bartera/conf/dev.conf
PORT=80

cd ~/code/bartera
play -Dconfig.file=$CONF -Dhttp.port=$PORT dist
rsync dist/bartera-$VERSION.zip dev.bartera.org:/home/michael/bartera

ssh dev.bartera.org <<ENDSSH

cd ~/bartera
unzip -of $DIR.zip
kill `cat current/RUNNING_PID`
rm current
ln -s $DIR current
nohup ./start

ENDSSH
