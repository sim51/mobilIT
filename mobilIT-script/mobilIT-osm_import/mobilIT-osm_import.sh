#!/bin/bash
###############################################################################
#
### DESCRIPTION
# Script to parse and import OSM data into neo4j.
#   * step 1 : download OSM file
#   * step 2 : filter OSM data to get only ways (osmosis has to be in the path : http://wiki.openstreetmap.org/wiki/Osmosis)
#   * step 3 : call Neo4j WS to do the import
#
### Version
# v1.0 : initialize
###############################################################################

###############################################################################
### Loading the environment file with the extention ".cfg" intead of ".sh"
###############################################################################
source  ./`basename $0 .sh`.cfg

###############################################################################
# CONSTANTS
###############################################################################
DATE=`date +%Y%m%d-%H%M`
WORK=$PWD

###############################################################################
# FUNCTIONS
###############################################################################
function retrieveOSMFile(){
  cd $WORK
  rm -rf download
  mkdir download
  cd download
  for j in "${OSM_FILE_URL[@]}"
    do
      wget $j
    done
}

function extract(){
  cd $WORK/download
  for file in `dir -d * | grep .bz2` ; do
    echo "Extract $file"
    bunzip2 $file
  done;
  rm -f *.bz2
}

function filter(){
  cd $WORK
  rm -rf filter
  mkdir filter
  cd download
  for file in `dir -d *` ; do
    echo "osmosis --read-xml $file --tag-filter accept-ways highway=* --write-xml ../filter/$file"
    osmosis --read-xml $file --tf accept-ways highway=* --tf reject-relations --tf reject-ways building=yes --write-xml ../filter/$file
  done
}

function neo4jImport(){
  cd $WORK/filter
  OSMFILES="files="
  for file in `dir -d *` ; do
    if [[ $OSMFILES == "files=" ]]
    then
      OSMFILES=$OSMFILES"$WORK/filter/$file"
    else
      OSMFILES=$OSMFILES"@$WORK/filter/$file"
    fi
    echo $OSMFILES
  done
  echo curl --data "$OSMFILES" $NEO4J_OSM_IMPORT_URL
  curl --data "$OSMFILES" $NEO4J_OSM_IMPORT_URL
}

###############################################################################
# PROGRAMME
###############################################################################
clear

echo "*****************************************************"
echo "   MOBILIT - OSM IMPORT "
echo "*****************************************************"
echo ""

echo "Step 1 : Download OSM file"
retrieveOSMFile
echo "Step 2 : Extract file"
extract
echo "Step 3 : Filter OSM data"
filter
echo "Step 4 : Import data to neo4j"
neo4jImport

exit 0
