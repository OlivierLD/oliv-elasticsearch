#!/bin/bash
echo -e "Inserting Master records"
#
ELASTIC_SEARCH_INSTANCE="localhost:9200"
#
# See what's in there, sanity check
echo -e "--------------------------------------"
echo -e "Before inserting, sanity check."
echo -e "--------------------------------------"
COMMAND="${ELASTIC_SEARCH_INSTANCE}/_cat/indices?v"
echo -e "Doing: curl \"${COMMAND}\""
curl "${COMMAND}"
#
DATA_INDEX="test-suites"
#
# Suggest delete if docs.count > 0
curl "${COMMAND}" | awk '{ print $7 }' > nb-docs.txt
DO_DELETE=false
LINE_NO=0
while read -r nb
do
  LINE_NO=$(( LINE_NO + 1))
  # echo -e "Reading line ${LINE_NO}"
  if [[ ( $LINE_NO -gt 1 && ${nb} -gt 0 ) ]]
  then
    DO_DELETE=true
  fi
done < nb-docs.txt
#
if [[ ${DO_DELETE} == true ]]
then
  echo -en "Do we delete index(es) [y|n] ? > "
  read -r hit
  #
  if [[ ${hit} =~ ^(yes|y|Y)$ ]]
  then
    KEEP_LOOPING=true
    while [[ "${KEEP_LOOPING}" == "true" ]]
    do
      echo -en "What index do we delete (empty [return] to break) ? > "
      read -r index
      if [[ "${index}" == "" ]]
      then
        KEEP_LOOPING=false
      else
        echo -e "--------------------------------------"
        echo -e "Deleting ${index}"
        echo -e "--------------------------------------"
        curl -X DELETE "${ELASTIC_SEARCH_INSTANCE}/${index}/" | jq
      fi
    done
  fi
fi
#
echo -en "Hit return to move on > "
read -r hit
#
DATA_TYPE="suites"
#
echo -e "--------------------------------------"
echo -e "Inserting record "
echo -e "--------------------------------------"
UNIQUE_INDEX="1"
RECORD_VALUE="{ \"id\": ${UNIQUE_INDEX}, \"name\": \"Master One\", \"value\": \"Duh\" }"
curl -X POST "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/${DATA_TYPE}/${UNIQUE_INDEX}" \
     -H "Content-Type: application/json" \
     -d "${RECORD_VALUE}" | jq
#
echo -en "Hit return to move on > "
read -r hit
#
echo -e "--------------------------------------"
echo -e "Inserting record "
echo -e "--------------------------------------"
UNIQUE_INDEX="2"
RECORD_VALUE="{ \"id\": ${UNIQUE_INDEX}, \"name\": \"Master Two\", \"value\": \"Duh\" }"
curl -X POST "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/${DATA_TYPE}/${UNIQUE_INDEX}" \
     -H "Content-Type: application/json" \
     -d "${RECORD_VALUE}" | jq
#
echo -en "Hit return to move on > "
read -r hit
#
echo -e "--------------------------------------"
echo -e "Inserted 2 master records, sanity check"
echo -e "--------------------------------------"
#
echo -en "Hit return to move on > "
read -r hit
#
# See what's in there, sanity check
COMMAND="${ELASTIC_SEARCH_INSTANCE}/_cat/indices?v"
echo -e "Doing: curl \"${COMMAND}\""
curl "${COMMAND}"
#
echo -e "--------------------------------------"
echo -e "Querying all:"
echo -e "--------------------------------------"
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/_search" | jq
echo -en "Hit return to move on > "
read -r hit
#
echo -e "--------------------------------------"
echo -e "Querying One, see the '_source':"
echo -e "--------------------------------------"
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/${DATA_TYPE}/1" | jq
echo -en "Hit return to move on > "
read -r hit
#
echo -en "Do we delete [y|n] ? > "
read -r hit
#
if [[ ${hit} =~ ^(yes|y|Y)$ ]]
then
  # To delete:
  echo -e "--------------------------------------"
  echo -e "Deleting all"
  echo -e "--------------------------------------"
  curl -X DELETE "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/" | jq
else
  # Now inserting details
  echo -e "Now inserting details."
  DATA_INDEX="test-cases"
  DATA_TYPE="cases"
  echo -e "--------------------------------------"
  echo -e "Inserting Detail"
  echo -e "--------------------------------------"
  MASTER_UNIQUE_INDEX="1"
  UNIQUE_INDEX="10"
  RECORD_VALUE="{ \"suite\": ${MASTER_UNIQUE_INDEX}, \"id\": ${UNIQUE_INDEX}, \"name\": \"Case One\", \"value\": \"I want a pizza\" }"
  curl -X POST "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/${DATA_TYPE}/${UNIQUE_INDEX}" \
       -H "Content-Type: application/json" \
       -d "${RECORD_VALUE}" | jq
  #
  echo -en "Hit return to move on > "
  read -r hit
  UNIQUE_INDEX="20"
  RECORD_VALUE="{ \"suite\": ${MASTER_UNIQUE_INDEX}, \"id\": ${UNIQUE_INDEX}, \"name\": \"Case Two\", \"value\": \"Get me a pizza now!\" }"
  curl -X POST "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/${DATA_TYPE}/${UNIQUE_INDEX}" \
       -H "Content-Type: application/json" \
       -d "${RECORD_VALUE}" | jq
  #
  echo -en "Hit return to move on > "
  read -r hit
  # A search
  COMMAND="${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/${DATA_TYPE}/_search"
  echo -e "Doing a curl -X GET ${COMMAND}"
  curl -X GET "${COMMAND}" | jq
  #
  echo -e "Also try :"
  echo -e "----------"
  echo -e "curl -X GET localhost:9200/test-cases,test-suites/_search | jq"
  echo -e "curl -X GET localhost:9200/test-*/_search | jq"
  echo -e "curl -X GET localhost:9200/test-cases/cases/_search | jq"
  echo -e "curl -X GET localhost:9200/test-cases/cases/_search?size=1&from=2 | jq"
  echo -e "curl -X GET localhost:9200/test-cases/cases/_search?q=name:*One | jq"
  #
  # Elasticsearch - The definitive guide, p 108, and after
  #
fi
#
echo -e "Done!"
