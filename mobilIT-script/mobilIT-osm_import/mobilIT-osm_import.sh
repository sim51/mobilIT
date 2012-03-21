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
function checkPreviousCMD(){
  if [ $? -ne 0 ]; then
    echo -e "\033[31m[KO]"
    tput sgr0
    exit 3
  else
    echo -e "\033[32m[OK]"
    tput sgr0
  fi
  echo
}

function retrieveOSMFile(){
  for j in "${OSM_FILE_URL[@]}"
    do
      cd $WORK
      rm -rf download
      mkdir download
      cd download
      wget $j
    done
}

function extract(){
  cd $WORK/download
  bunzip2 *
  rm -f *.bz2
}

function filter(){
  cd $WORK
  rm -rf filter
  mkdir filter
  cd download
  for file in `dir -d *` ; do
    osmosis --read-xml $file --tag-filter accept-ways highway=* --write-xml ../filter/$file
  done
  rm -rf $WORK/download
}

function neo4jImport(){
  cd $WORK/filter
  for file in `dir -d *` ; do
    osmFile = "osmFile[]=" + $file
  done
  wget --post-data="$osmFiles" $NEO4J_OSM_IMPORT_URL
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
