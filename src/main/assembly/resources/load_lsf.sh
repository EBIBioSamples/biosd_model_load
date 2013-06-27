#!/bin/sh
# 
# Runs multiple loader (load.sh) instances in parallel through the EBI's LSF cluster.
# TODO: THIS IS TO BE TESTED (requires the summary-exporting code)
# 

MYDIR=$(dirname "$0")
cd "$MYDIR"

if [ "$1" == "--help" ]; then
  cat <<EOT

  ********* WARNING THIS HASN'T BEEN TESTED YET *********

  usage: $0 [summary-file]
  
Loads all the files listed in the standard input. Loading results will be reported in a summary file.

This command is affected by the environment variables:

  BIOSD_LOAD_SAMPLING_RATIO, if it is less than 100, only a random subset of the 
  submissions in the target directory is loaded.
  
  
EOT
  exit 1
fi

outfpath=$1
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

# Pick a random sample if there is this variable defined, 100% otherwise.	 
if [ "$SAMPLING_RATIO" == "" ]; then SAMPLING_RATIO=100; fi
	 
printf "FILE\tEXCEPTION\tMESSAGE\tN_ITEMS\tPARSING_TIME\tPERSISTENCE_TIME\n" >$outfpath

(while read fpath
do
  if [ $[ $RANDOM % 100 ] -ge $SAMPLING_RATIO ]; then continue; fi
	wfpath=$(echo "$fpath"| sed s/'\/'/'_'/g)
  echo bsub -K -oo /dev/null -J $wfpath ./load.sh $fpath
done) | xargs -d '\n' -P 100 -n 1 --replace=_cmd_ -- bash -c "_cmd_; exit 0" 

echo
echo 'All Finished.'
echo
echo
