#!/bin/sh
#


inputdir=$1

if [ "$inputdir" == "" ]; then
  cat <<EOT

  usage: $0 input-dir [summary-file]
  
Tests the loading of all files in a given directory. You need to first issue 'mvn -P<proper-profile> test-compile'  
with a profile corresponding to your database target. The script will try to load sampletab files (.sampletab.txt extension)
one at a time and will report the results in a summary file.

  input-dir     = path where to load files from, all *.sampletab.txt files are considered, recursively
  summary-file  = file where to store summary
  

EOT
  exit 1
fi

outfpath=$2
if [ "$outfpath" == "" ]; then 
  outfpath = "load_test_results.txt"
fi

if [ -e "$outfpath" ]; then
	cat <<EOT
	
	  Cowardly refusing to overwrite "$outfpath", delete or rename this file before passing its name to me 
	  (or pass me another one)
EOT
fi
	 
echo FILE EXCEPTION MESSAGE >$outfpath
for fpath in $(find $inputdir -type f -name '*.sampletab.txt' -or -name 'sampletab.txt' )
do
  wfpath=$(echo "$fpath"| sed s/'\/'/'_'/g)
  mvn exec:java \
    -Ptest.oracle_dev \
    -DargLine="-Xms1G -Xmx4G -XX:PermSize=256m -XX:MaxPermSize=2G" \
    -Dexec.mainClass=uk.ac.ebi.fg.biosd.sampletab.test.LoadTestCmd \
    -Dexec.classpathScope=test \
    -Dexec.arguments="$fpath" \
    2>&1 | tee "target/load_test_$wfpath".out

  . /tmp/biosd.loadTest_out.sh
	rm -f /tmp/biosd.loadTest_out.sh
	
  echo $fpath $BIOSD_LOAD_RESULT_EXCEPTION  "$BIOSD_LOAD_RESULT_MESSAGE" >>$outfpath
done
