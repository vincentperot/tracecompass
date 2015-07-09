#!/bin/bash
output=`mvn versions:display-plugin-updates -f ../pom.xml`
#filter only updates and show unique
summary=`echo "${output}" | grep "\\->" | sort | uniq`
echo -e "Summary:\n${summary}"
#remove empty lines and count lines
outdatedNb=`echo "${summary}" | sed '/^\s*$/d' | wc -l`
echo Number of outdated plugins: "${outdatedNb}"
exit ${outdatedNb}
