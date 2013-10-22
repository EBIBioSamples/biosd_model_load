#!/bin/sh
#
# Runs the loading test (load_test.sh) through the EBI's LSF cluster.
#

MYDIR=$(dirname "$0")
cd "$MYDIR"/..

if [ "$1" == "--help" ]; then
  cat <<EOT

DEPRECATED!!! Please build the command line package and use load_lsf.sh instead

  usage: cat <sample-tab-paths> | $0 [summary-file]
  
Tests the loading of all files listed in the standard input. You need to first issue 'mvn -P<proper-profile> test-compile'  
with a profile corresponding to your database target. Loading Results are reported in a summary file.
  
This command is affected by the environment variable SAMPLING_RATIO, if it is less than 100, only a random subset of the 
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

bsub_out_path=target/load_test_${wfpath}.out
bsub_out_path=/dev/null
	 
printf "FILE\tEXCEPTION\tMESSAGE\tN_ITEMS\tPARSING_TIME\tPERSISTENCE_TIME\n" >$outfpath

(while read fpath
do
  if [ $[ $RANDOM % 100 ] -ge $SAMPLING_RATIO ]; then continue; fi
	wfpath=$(echo "$fpath"| sed s/'\/'/'_'/g)
  echo bsub -K -oo "$bsub_out_path" -J $wfpath ./test_scripts/load_test_cmd.sh $fpath $outfpath
done) | xargs -d '\n' -P 50 -n 1 --replace=_cmd_ -- bash -c "_cmd_; exit 0" 

echo
echo 'All done.'
echo
echo
