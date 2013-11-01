#!/bin/sh
# 
# Runs multiple loader (load.sh) instances in parallel through the EBI's LSF cluster.
# TODO: documentation
# 

MYDIR=$(dirname "$0")
cd "$MYDIR"

if [ "$1" == "--help" ]; then
  cat <<EOT

  ********* WARNING THIS HASN'T BEEN TESTED YET *********

  usage: $0 < sample_tab_paths
  
Loads all the files listed in the standard input, using and LSF cluster and its commands. Loading results will possibly
be reported in the loading_diagnostics in the target database (see below).

This command is affected by the environment variables and Java options:

  BIOSD_LOAD_SAMPLING_RATIO, if it is less than 100, only a random subset of the 
  submissions in the target directory is loaded.
  
  OPTS="\$OPTS -Duk.ac.ebi.fg.biosd.sampletab.loader.debug=true"
  causes the loader to record some diagnostic data into the target database (in the loading_diagnostics table)
  
EOT
  exit 1
fi

# Pick a random sample if there is this variable defined, 100% otherwise.	 
if [ "$BIOSD_LOAD_SAMPLING_RATIO" == "" ]; then BIOSD_LOAD_SAMPLING_RATIO=100; fi
	 
(while read fpath
do
  if [ $[ $RANDOM % 100 ] -ge $BIOSD_LOAD_SAMPLING_RATIO ]; then continue; fi
	wfpath=$(echo "$fpath"| sed s/'\/'/'_'/g)
  echo bsub -K -oo /dev/null -J $wfpath ./load.sh $fpath
done) | xargs -d '\n' -P 100 -n 1 --replace=_cmd_ -- bash -c "_cmd_; exit 0" 

echo
echo 'All Finished.'
echo
echo
