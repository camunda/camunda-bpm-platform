#!/bin/sh
​
JBOSS_HOME="$(dirname "$0")/../server/wildfly-29.0.0.Final"
echo "$JBOSS_HOME";
​
CONFIG=widlfly-config.properties
if [ "$1" ]
then
 CONFIG=$1
fi
​
echo starting Wildfly in admin-only mode
/bin/sh "$JBOSS_HOME/bin/standalone.sh" --admin-only &
​
sleep 10

echo updating Wildfly configuration
./$JBOSS_HOME/bin/jboss-cli.sh -c --file=wildfly-config.cli --properties=$CONFIG
​
echo configuration updated, shutting down server
#kill $(cat "$!")

