#!/bin/sh
# 
# Unload multiple loader (load.sh) instances in parallel through an LSF cluster (we use the EBI one).
# TODO: documentation
# 

MYDIR=$(dirname "$0")
cd "$MYDIR"

if [ "$1" == "--help" ]; then
  cat <<EOT

  usage: $0 < submission accessions
  
Remove all the submissions listed in the standard input, using and LSF cluster and its commands. 

This command is affected by the environment variables and Java options:
  
  export LOADER_OPTS='...'
  allows to send more options to unload.sh, (e.g., --purge)
 
  export BIOSD_LSF_GROUP='...'
  the LSF job group that is used to limit the number of jobs that are running at any time. 
  Default is 'BioSDLoader', examples might be BioSDLoaderDev, BioSDLoaderTest. '/' is automatically added.  

EOT
  exit 1
fi

# The LSF group used for loading jobs
if [ "$BIOSD_LSF_GROUP" == '' ]; then BIOSD_LSF_GROUP='BioSDLoader'; fi

# If it doesn't already exist, create an LSF group to manage a limited running pool
if [ "$(bjgroup -s /$BIOSD_LSF_GROUP 2>&1)" == 'No job group found' ]; then
  bgadd -L 100 /$BIOSD_LSF_GROUP
fi

# Submit all jobs, into a group with a bound on no. of running jobs
# 
while read acc
do
  bsub -g /$BIOSD_LSF_GROUP -oo /dev/null -J "$acc" ./unload.sh $LOADER_OPTS "$acc"
done

# Now poll the LSF and wait until all the jobs terminate.
echo 'All the jobs submitted, now waiting for their termination, please be patient.'
while [ "$(bjobs -g /$BIOSD_LSF_GROUP 2>&1)" != 'No unfinished job found' ]
do
  sleep 1m
done

echo
echo 'All Finished.'
echo
echo
