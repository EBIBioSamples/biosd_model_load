#!/bin/sh

# This is the entry point that invokes the SampleTAB Loader command-line.
#Ê
cd "$(dirname $0)"
MYDIR="$(pwd)"

# See here for an explaination about ${1+"$@"} :
# http://stackoverflow.com/questions/743454/space-in-java-command-line-arguments 

./generic_invoker.sh uk.ac.ebi.fg.biosd.sampletab.UnloaderCmd ${1+"$@"}
