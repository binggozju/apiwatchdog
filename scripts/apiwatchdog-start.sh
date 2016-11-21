#!/bin/bash
# -------------------------------------------------------------------------
# Usage: used to configure the parameters of JVM and apiwatchdog, start
#	apiwatchdog service
# -------------------------------------------------------------------------

if [ $# -ne 1 ]; then
	echo "Usage: $0 local|pre-release|production"
	exit 1
fi

ENV=$1
case $ENV in
	local|pre-release|production)
		;;

	*)
		echo "Error: invalid parameter"
		echo "Usage: $0 local|pre-release|production"
		exit 1
		;;
esac

# kill the old apiwatchdog process
ps -ef | grep apiwatchdog.jar | grep -v 'grep' | awk '{print $2}' | xargs -I {} kill -9 {}

DIR=$(dirname `readlink -m $0`)
cd $DIR/..

# configuration of JVM
#JAVA_OPTS="-Xms200m -Xmx512m"
JAVA_OPTS=
echo "JAVA_OPTS: ${JAVA_OPTS}"

# configuration of apiwatchdog
APIWATCHDOG_OPTS="--spring.config.location=config/apiwatchdog.properties,config/${ENV}.properties"
echo "APIWATCHDOG_OPTS: $APIWATCHDOG_OPTS"
if [ ! -f config/${ENV}.properties ]; then
	echo "${ENV}.properties not found, exit"
	exit 1
fi

# start apiwatchdog with embeded tomcat
if [ -f target/apiwatchdog.jar ]; then
	exec java $JAVA_OPTS -jar target/apiwatchdog.jar $APIWATCHDOG_OPTS
elif [ -f libs/apiwatchdog.jar ]; then
	exec java $JAVA_OPTS -jar libs/apiwatchdog.jar $APIWATCHDOG_OPTS
else
	echo "Error: apiwatchdog.jar not found"
	exit 1
fi
