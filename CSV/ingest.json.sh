#!/bin/bash
#
ELASTIC_SEARCH_INSTANCE="localhost:9200"
#
# Transform CSV to JSON
#
pushd ..
./gradlew shadowJar
java -jar build/libs/elastic-search-1.0-all.jar CSV/NYC_Transit_Subway_Entrance_And_Exit_Data.csv CSV/NYC_Transit_Subway_Entrance_And_Exit_Data.json
popd
#
while read f1
do
#   echo -e "${f2}"
#        -u elastic:XXXX \  # User identification, if needed
   curl -X POST "${ELASTIC_SEARCH_INSTANCE}/subway_info_v2/station" \
        -H "Content-Type: application/json" \
        -d "$f1"
done < NYC_Transit_Subway_Entrance_And_Exit_Data.json
#
# See what's in there, sanity check
curl "${ELASTIC_SEARCH_INSTANCE}/_cat/indices?v"
#
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v2" | jq
#
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v2/station/1" | jq
