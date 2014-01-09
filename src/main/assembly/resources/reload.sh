#!/bin/sh

cat <<EOT

			***** !!! DEPRECATED !!! **** 

Use load.sh --update in place of this command, it's much more efficient. This script will be removed soon.

EOT

cd "$(dirname $0)"
MYDIR="$(pwd)"

FILENAME="$1"

# this should be an absolute path
echo "FILENAME = "$FILENAME 


SUBMISSIONIDENTIFIER=`grep 'Submission Identifier' $FILENAME | sed 's|Submission Identifier\s\([A-Za-z\-]*\)|\1|'`

echo "SUBMISSIONIDENTIFIER = "$SUBMISSIONIDENTIFIER

time ./unload.sh $SUBMISSIONIDENTIFIER
echo "unload exit code = "$?

time ./load.sh $FILENAME
echo "load exit code = "$?
