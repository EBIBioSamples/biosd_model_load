#!/bin/sh

# Deploys the command line binary. This doesn't do much, but it's needed with Bamboo.
# 

MYDIR=$(dirname "$0")
cd "$MYDIR"/target

target="$1"

if [ "$target" == "" ]; then
  target=.
fi;

echo
echo 
echo _______________ Deploying the Command Line Binary to $target _________________

yes A| unzip biosd_loader_shell_*.zip -d "$target"

echo ______________________________________________________________________________
echo
echo
echo
