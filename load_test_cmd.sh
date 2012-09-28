#!/bin/sh

# Used for load_test_lsf.sh
#

fpath="$1"
outfpath="$2"

pid=$$

mvn exec:java \
  -Ptest.oracle_dev \
	-DargLine="-Xms1G -Xmx4G -XX:PermSize=256m -XX:MaxPermSize=2G" \
	-Dexec.mainClass=ac.uk.ebi.fg.biosd.sampletab.test.LoadTestCmd \
	-Dexec.classpathScope=test \
	-Dexec.args="$fpath $pid" 
		  
. target/biosd.loadTest_out_$$.sh
rm -f target/biosd.loadTest_out_$$.sh

echo "$fpath" $BIOSD_LOAD_RESULT_EXCEPTION  "$BIOSD_LOAD_RESULT_MESSAGE"	$N_ITEMS	$PARSING_TIME	$PERSISTENCE_TIME >>$outfpath
