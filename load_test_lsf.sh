#!/bin/sh
#Ê
#ÊRuns the loading test (load_test.sh) through the EBI's LSF cluster.
#Ê

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

(for fpath in $(find $inputdir -type f -name '*.sampletab.txt' -or -name 'sampletab.txt' )
do
	wfpath=$(echo "$fpath"| sed s/'\/'/'_'/g)
  echo bsub -K -oo target/load_test_${wfpath}.out -J $wfpath ./load_test_cmd.sh $fpath $outfpath
done) | xargs -d '\n' -P 100 -n 1 --replace=_cmd_ -- bash -c "_cmd_; exit 0" 

echo
echo 'All done.'
echo
echo
