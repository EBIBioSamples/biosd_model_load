#!/bin/sh

#�This is the entry point that invokes the SampleTAB Loader command-line.
#�
cd "$(dirname $0)"
MYDIR="$(pwd)"

#�See here for an explaination about ${1+"$@"} :
# http://stackoverflow.com/questions/743454/space-in-java-command-line-arguments 

./generic_invoker.sh ac.uk.ebi.fg.biosd.sampletab.LoaderCmd ${1+"$@"}
