#!/bin/bash
###############################################################################
#
### DESCRIPTION
# Example of bash script
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

function helloWorld(){
  echo "Hello World"
}

###############################################################################
# PROGRAMME
###############################################################################
#clear

echo "*****************************************************"
echo " EXAMPLE SCRIPT "
echo "*****************************************************"
echo ""

helloWorld
checkPreviousCMD

exit 0