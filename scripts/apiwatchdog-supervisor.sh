#!/bin/bash
# ---------------------------------------------------------------------------
# Description: supervisor apiwatchdog's running status.
#   add to crontab: Crontab: */2 * * * *  /your/path/apiwatchdog-supervisor.sh
# ---------------------------------------------------------------------------

# configuration of log file
SUPERVISOR_LOG_FILE=/data0/logs/apiwatchdog/supervisor.log
APIWATCHDOG_LOG_FILE=/dev/null

DIR=$(dirname `readlink -m $0`)
cd $DIR/..

function log() {
    local log_level=$1
    local log_msg=$2
    local date_str=$(date +"%Y-%m-%d %H:%M:%S")
    echo "[$date_str] [$log_level] $log_msg" >> $SUPERVISOR_LOG_FILE
}

EXIST=$(ps aux | grep "apiwatchdog.properties" | grep -v grep | wc -l)
if [ $EXIST -gt 0 ]; then
	log INFO "apiwatchdog is running"
else
    log ERROR "apiwatchdog has stopped"
    nohup ./scripts/apiwatchdog-start.sh > $APIWATCHDOG_LOG_FILE 2>&1 &
    log INFO "apiwatchdog-supervisor has restarted apiwatchdog"
fi
