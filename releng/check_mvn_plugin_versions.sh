#!/bin/bash

# Point ourselves to the script's directory (so it can be run "out-of-tree")
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
output=`mvn versions:display-plugin-updates -f $DIR/../pom.xml`

#filter only updates and show unique
summary=`echo "${output}" | grep "\\->" | sort | uniq`

#remove empty lines and count lines
echo -e "Summary:\n${summary}"
outdatedNb=`echo "${summary}" | sed '/^\s*$/d' | wc -l`
echo Number of outdated plugins: "${outdatedNb}"
exit ${outdatedNb}
