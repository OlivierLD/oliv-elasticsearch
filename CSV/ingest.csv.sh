#!/bin/bash
#
ELASTIC_SEARCH_INSTANCE="localhost:9200"
#
while read f1
do
   # Replace double quotes with simple quotes
   f2=$(echo ${f1} | sed "s/\"/'/g")
#   echo -e "${f2}"
#        -u elastic:XXXX \  # User identification, if needed
#                                          /indexName     /typeName/uniqueId
   curl -X POST "${ELASTIC_SEARCH_INSTANCE}/subway_info_v1/station" \
        -H "Content-Type: application/json" \
        -d "{ \"station\": \"$f2\" }"
done < NYC_Transit_Subway_Entrance_And_Exit_Data.csv
#
# See what's in there, sanity check
curl "${ELASTIC_SEARCH_INSTANCE}/_cat/indices?v"
#
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v1" | jq
#
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v1/station/1" | jq


