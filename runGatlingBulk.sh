#! /bin/bash

###### General bash settings to allow easier debugging of this script#####
# Uncomment this to debug script
# set -x

set -e          # Fail fast if an error is encountered
set -o pipefail # Look at all commands in a pipeline to detect failure, not just the last
set -o functrace # Allow tracing of function calls
set -E          # Allow ERR signal to always be fired
##########################################################################


####### Error Handling ################
failure() {
    local lineno=$1
    local msg=$2
    echo "Failed at $lineno: $msg"
}
trap 'failure ${LINENO} "$BASH_COMMAND"' ERR
##########################################################################


############################# Arg parsing ############################################################
usage() { echo "$0 usage:" && grep " .)\ #" $0; exit 0; }

rps=10
url="http://localhost:9001"
payload="ai-api-bulk-local.json"

options=':p:c:u:r:h'
while getopts $options option
do
    case $option in
        r) # Specify concurrent users
            rps=${OPTARG}
            ;;
        p) # Payload name
            payload=${OPTARG}
            ;;
        u) # Specify Base URL
            url=${OPTARG}
            ;;
        c) # Specify configFileName
            configFileName=${OPTARG}
            ;; #configname
        h  ) usage; exit;;
        \? ) echo "Unknown option: -$OPTARG" >&2; exit 1;;
        :  ) echo "Missing option argument for -$OPTARG" >&2; exit 1;;
        *  ) echo "Unimplemented option: -$OPTARG" >&2; exit 1;;
    esac
done
shift $((OPTIND - 1))

if [ "x" = "x$configFileName" ]; then
    echo "configFileName must be specified"
    exit
fi
echo "Setting rps to $rps"
echo "Setting url to $url"
echo "Setting payload to $payload"
echo "Setting configFileName to $configFileName"

#########################################################################################

echo "Beginning Gatling tests"
JAVA_OPTS="-DCONCURRENT_USERS=${rps} -DPAYLOAD_NAME=${payload} -DBASE_URL=${url} -DCONFIG_NAME=${configFileName}" \
    sbt "project address-index-server" "gatling-it:testOnly uk.gov.ons.gatling.simulations.RegistersSimulationClosedModel" \
    "gatling-it:lastReport"
