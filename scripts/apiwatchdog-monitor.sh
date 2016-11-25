#!/bin/bash
# ---------------------------------------------------------------------------
# Description: monitor apiwatchdog's running status.
#   add to crontab: Crontab: */1 * * * *  /your/path/apiwatchdog-monitor.sh
# ---------------------------------------------------------------------------

# configuration of log file
MONITOR_LOG_HOME=/data0/apiwatchdog/monitor
[ -d $MONITOR_LOG_HOME ] || mkdir -p $MONITOR_LOG_HOME
MONITOR_LOG_FILE=$MONITOR_LOG_HOME/monitor.log
APIWATCHDOG_LOG_FILE=/dev/null

DIR=$(dirname `readlink -m $0`)
cd $DIR/..

function log() {
    local log_level=$1
    local log_msg=$2
    local date_str=$(date +"%Y-%m-%d %H:%M:%S")
    echo "[$date_str] [$log_level] $log_msg" >> $SUPERVISOR_LOG_FILE
}

EXIST=$(ps aux | grep "apiwatchdog.jar" | grep -v grep | wc -l)
if [ $EXIST -gt 0 ]; then
	log INFO "apiwatchdog is running"
else
    log ERROR "apiwatchdog has stopped"
    nohup ./scripts/apiwatchdog-start.sh production 2>&1 $APIWATCHDOG_LOG_FILE &
    log INFO "apiwatchdog-monitor.sh has restarted apiwatchdog"
fi
