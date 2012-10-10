#!/bin/sh

# Used for load_test_lsf.sh
#

fpath="$1"
outfpath="$2"

pid=$$

mvn exec:java \
  -Ptest.oracle_test \
	-DargLine="-Xms1G -Xmx4G -XX:PermSize=256m -XX:MaxPermSize=2G" \
	-Dexec.classpathScope=test \
	-Dexec.mainClass=uk.ac.ebi.fg.biosd.sampletab.test.LoadTestCmd \
	-Dexec.args="$fpath $pid" 
		  
. target/biosd.loadTest_out_$$.sh
rm -f target/biosd.loadTest_out_$$.sh

printf "$fpath\t$BIOSD_LOAD_RESULT_EXCEPTION\t$BIOSD_LOAD_RESULT_MESSAGE\t$N_ITEMS\t$PARSING_TIME\t$PERSISTENCE_TIME\n" >>$outfpath
