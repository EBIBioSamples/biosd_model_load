#!/bin/sh

# This is the entry point that invokes the unloader and then the loader
#

cd "$(dirname $0)"
MYDIR="$(pwd)"

FILENAME=$1

# this should be an absolute path
echo "FILENAME = "$FILENAME 


SUBMISSIONIDENTIFIER=`grep 'Submission Identifier' $FILENAME | sed 's|Submission Identifier\s\([A-Za-z\-]*\)|\1|'`

echo "SUBMISSIONIDENTIFIER = "$SUBMISSIONIDENTIFIER

time ./unload.sh $SUBMISSIONIDENTIFIER
echo "unload exit code = "$?

time ./load.sh $FILENAME
echo "load exit code = "$?
