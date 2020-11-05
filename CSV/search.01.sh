#!/bin/bash
#
ELASTIC_SEARCH_INSTANCE="localhost:9200"
#
# See what's in there, sanity check
echo -e "Sanity check, should be yellow"
echo -e "------------------------------"
curl "${ELASTIC_SEARCH_INSTANCE}/_cat/indices?v"
#
echo -e "------------------"
echo -e ">> Query on data set"
echo -e "------------------"
echo -en "Hit return to move on"
read hit
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v1" | jq
#
echo -e "-----------------------"
echo -e ">> Search 1, no parameters"
echo -e "-----------------------"
echo -en "Hit return to move on"
read hit
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v1/station/_search" | jq
#
# With query parameters
echo -e "-------------------------------"
echo -e ">> Search 2, with query parameters"
echo -e "-------------------------------"
echo -en "Hit return to move on"
read hit
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v1/station/_search" \
     -H "Content-Type: application/json" \
     -d "{ \"query\": { \"match_all\": {} }}" | jq
#
echo -e "--------------------------------------------------------"
echo -e ">> Search 3, with query parameters and sort. Should fail..."
echo -e "--------------------------------------------------------"
echo -en "Hit return to move on"
read hit
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v1/station/_search" \
     -H "Content-Type: application/json" \
     -d "{ \"query\": { \"match_all\": {} }, \"sort\": [ { \"station\": \"asc\" } ] }" | jq
#
echo -e "-----------------------------------------------"
echo -e ">> Search 4, with query parameters, limit and size (first line)"
echo -e "-----------------------------------------------"
echo -en "Hit return to move on"
read hit
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v1/station/_search" \
     -H "Content-Type: application/json" \
     -d "{ \"query\": { \"match_all\": {} }, \"from\": 1, \"size\": 1 }" | jq
#
echo -e "-----------------------------------------------"
echo -e ">> Search 5, with query parameters, limit and size (big!)"
echo -e "-----------------------------------------------"
echo -en "Hit return to move on"
read hit
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/subway_info_v1/station/_search" \
     -H "Content-Type: application/json" \
     -d "{ \"query\": { \"match_all\": {} }, \"from\": 10, \"size\": 200 }" | jq
#
echo -e "That's it!"
