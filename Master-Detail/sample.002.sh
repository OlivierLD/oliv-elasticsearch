#!/bin/bash
echo -e "More samples..., targeted update."
#
# Define a timestamp function, not sure we want that one.
timestamp() {
  date +"%T" # current time
}
#
# alias ts='echo $(date +%s)'
#
ELASTIC_SEARCH_INSTANCE="localhost:9200"
#
# See what's in there, sanity check
echo -e "--------------------------------------"
echo -e "Before inserting, sanity check."
echo -e "--------------------------------------"
COMMAND="${ELASTIC_SEARCH_INSTANCE}/"
echo -e "Doing: curl \"${COMMAND}\""
echo -e "--------------------------------------"
echo -e ">> ES Version $(curl -X GET "${COMMAND}" | jq '.version.number')"
echo -e "--------------------------------------"
#
COMMAND="${ELASTIC_SEARCH_INSTANCE}/_cat/indices?v"
echo -e "Doing: curl \"${COMMAND}\""
curl -X GET "${COMMAND}"
echo -e "--------------------------------------"
#
DATA_INDEX=roule-ma-poule
DATA_TYPE=the-type
#
echo -en "Cleanup ? > "
read -r resp
if [[ ${resp} =~ ^(yes|y|Y)$ ]]
then
  curl -X DELETE "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/" | jq
  echo -en "Done. Hit return to move on > "
  read -r hit
fi
#
echo -e "Now inserting bulk records"
#
NB_REC=20   # 100
echo -e "--------------------------------------"
echo -e "Inserting ${NB_REC} records "
echo -e "--------------------------------------"
#
LINE_NO=0
#
while [[ $LINE_NO -lt ${NB_REC} ]]
do
  LINE_NO=$(( LINE_NO + 1 ))
  UNIQUE_INDEX=$(date +%s)
  echo -e "Inserting record #${LINE_NO}, id ${UNIQUE_INDEX}"
  #
  RECORD_VALUE="{ \"pk\": ${UNIQUE_INDEX}, \"data\": \"Rec-${UNIQUE_INDEX}\" }"
  curl -X POST "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/${DATA_TYPE}/${UNIQUE_INDEX}" \
       -H "Content-Type: application/json" \
       -d "${RECORD_VALUE}" | jq
  sleep 1
done
#
echo -e "Check it out: curl -X GET \"${ELASTIC_SEARCH_INSTANCE}/roule*/_search\" | jq"
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/roule*/_search" | jq
#
echo -en "Enter the 'pk' of the record to update > "
read -r pk
echo -e "Now doing a curl -X GET \"${ELASTIC_SEARCH_INSTANCE}/_search\" -H 'Content-Type: application/json' -d'{ \"query\": { \"match\": { \"pk\": ${pk} }}}' | jq"
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/_search" -H 'Content-Type: application/json' -d "{ \"query\": { \"match\": { \"pk\": ${pk} }}}" > found.json
cat found.json | jq
_id=$(cat found.json | jq '.hits.hits[0]._id')
echo -e "Will update _id: ${_id}"
RECORD_VALUE="{ \"pk\": ${pk}, \"data\": \"Updated Rec-${pk}\" }"
curl -X PUT "${ELASTIC_SEARCH_INSTANCE}/${DATA_INDEX}/${DATA_TYPE}/${pk}" \
     -H "Content-Type: application/json" \
     -d "${RECORD_VALUE}" | jq
echo -en "Update done, let's check, hit return"
read -r hit
curl -X GET "${ELASTIC_SEARCH_INSTANCE}/_search" -H 'Content-Type: application/json' -d "{ \"query\": { \"match\": { \"pk\": ${pk} }}}" > new.json
cat new.json | jq
echo -e "New record:"
jq '.hits.hits[0]._source' new.json

