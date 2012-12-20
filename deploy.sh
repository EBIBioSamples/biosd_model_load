#!/bin/sh

#ÊDeploys the command line binary. This doesn't do much, but it's needed with Bamboo.
#Ê

MYDIR=$(dirname "$0")
cd "$MYDIR"/target

target="$1"

if [ "$target" == "" ]; then
  target=target
fi;

echo
echo 
echo _______________ Deploying the Command Line Binary to $target _________________

yes A| unzip biosd_loader_shell_*.zip -d /ebi/microarray/home/biosamples-dev/loader_relational/dev

echo ______________________________________________________________________________
echo
echo
echo
