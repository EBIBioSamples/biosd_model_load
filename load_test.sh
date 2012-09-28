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
if [ "$outfpath" == "" ]; then 
  outfpath="load_test_results.csv"
fi

if [ -e "$outfpath" ]; then
	cat <<EOT
	
	  Cowardly refusing to overwrite "$outfpath", delete or rename this file before passing its name to me 
	  (or pass me another one)
EOT
  exit 1
fi
	 
echo FILE EXCEPTION MESSAGE	N_ITEMS	PARSING_TIME	PERSISTENCE_TIME >$outfpath
for fpath in $(find $inputdir -type f -name '*.sampletab.txt' -or -name 'sampletab.txt' )
do
	wfpath=$(echo "$fpath"| sed s/'\/'/'_'/g)
  ./load_test_cmd.sh "$fpath" "$outfpath" 2>&1 | bz2 "target/load_test_${wfpath}.out.bz2"
	
  echo "$fpath" $BIOSD_LOAD_RESULT_EXCEPTION  "$BIOSD_LOAD_RESULT_MESSAGE"	$N_ITEMS	$PARSING_TIME	$PERSISTENCE_TIME >>$outfpath
done
