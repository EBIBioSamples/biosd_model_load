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
UNLOADEXIT = $?
echo "unload exit code = "$UNLOADEXIT

time ./load.sh $FILENAME
LOADEXIT = $?
echo "load exit code = "$LOADEXIT

if [ $UNLOADEXIT -gt $LOADEXIT ];
then
	exit $UNLOADEXIT 
else
	exit $LOADEXIT
fi