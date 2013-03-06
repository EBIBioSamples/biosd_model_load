#!/bin/sh

source_dir=$1
ratio=100;

if [ "$source_dir" == '-r' ]; then
  ratio=$2
  source_dir=$3;
fi

if [ "$source_dir" == "" ]; then
	cat <<EOT


  usage: $0 [-r 0-100] <target-dir>
  
Loads a random sample (size specified by -r) of sampletab files from the target dir.
It uses the SampleTab command line loader, located at $SAMPLETAB_LOADER_CMD ('./loader.sh' by default).
 
EOT
exit 1
fi

if [ "$SAMPLETAB_LOADER_CMD" == '' ]; then SAMPLETAB_LOADER_CMD='./loader.sh'; fi

cat <<EOT


Sampling $ratio% of files in "$source_dir", using the command "$SAMPLETAB_LOADER_CMD"


EOT

for fname in $(find "$source_dir" -type f -and \( -name '*sampletab.txt' -or -name 'sampletab.txt' \) )
do
  if [ $[ $RANDOM % 100 ] -gt $ratio ]; then continue; fi

  "$SAMPLETAB_LOADER_CMD" "$fname"  
done  
