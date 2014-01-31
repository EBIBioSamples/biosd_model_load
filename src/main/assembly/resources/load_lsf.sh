#!/bin/sh
# 
# Runs multiple loader (load.sh) instances in parallel through the EBI's LSF cluster.
# TODO: documentation
# 

MYDIR=$(dirname "$0")
cd "$MYDIR"

if [ "$1" == "--help" ]; then
  cat <<EOT

  usage: $0 < sample_tab_paths
  
Loads all the files listed in the standard input, using and LSF cluster and its commands. Loading results will possibly
be reported in the loading_diagnostics in the target database (see below).

This command is affected by the environment variables and Java options:

  export BIOSD_LOAD_SAMPLING_RATIO, if it is less than 100, only a random subset of the 
  submissions in the target directory is loaded.
  
  export OPTS="\$OPTS -Duk.ac.ebi.fg.biosd.sampletab.loader.debug=true"
  causes the loader to record some diagnostic data into the target database (in the loading_diagnostics table)
  
  export LOADER_OPTS='...'
  allows to send more options to load.sh, together with the file path (e.g., --update)
 
  export BIOSD_LSF_GROUP='...'
  the LSF job group that is used to limit the number of jobs that are running at any time. 
  Default is 'BioSDLoader', examples might be BioSDLoaderDev, BioSDLoaderTest. '/' is automatically added.  

EOT
  exit 1
fi

# Pick a random sample if there is this variable defined, 100% otherwise.	 
if [ "$BIOSD_LOAD_SAMPLING_RATIO" == "" ]; then BIOSD_LOAD_SAMPLING_RATIO=100; fi

# The LSF group used for loading jobs
if [ "$BIOSD_LSF_GROUP" == '' ]; then BIOSD_LSF_GROUP='BioSDLoader'; fi

# If it doesn't already exist, create an LSF group to manage a limited running pool
if [ "$(bjgroup -s /$BIOSD_LSF_GROUP 2>&1)" == 'No job group found' ]; then
  bgadd -L 100 /$BIOSD_LSF_GROUP
fi

# Submit all jobs, into a group with a bound on no. of running jobs
# 
while read fpath
do
  if [ $[ $RANDOM % 100 ] -ge $BIOSD_LOAD_SAMPLING_RATIO ]; then continue; fi
	wfpath=$(echo "$fpath"| sed s/'\/'/'_'/g)
  bsub -g /$BIOSD_LSF_GROUP -oo /dev/null -J "$wfpath" ./load.sh $LOADER_OPTS "$fpath"
done

# Now poll the LSF and wait until all the jobs terminate.
echo 'All the jobs submitted, now waiting for their termination, please be patient.'
while [ "$(bjobs -g /$BIOSD_LSF_GROUP 2>&1)" != 'No unfinished job found' ]
do
  sleep 5m
done

echo
echo 'All Finished.'
echo
echo
