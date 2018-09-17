#!/bin/bash

if [ -z ${1+x} ]; 
then echo "Must pass in the remote url"; exit -1; 
else url=$1; echo "url is set to $url"; 
fi


./runGatlingBulk.sh -c "ai-api-bulk-full" -u "$1"
