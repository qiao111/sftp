#!/bin/sh

PRG="$0"
PRGDIR=`dirname "$PRG"`

export CATALINA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9129"

$PRGDIR/startup.sh