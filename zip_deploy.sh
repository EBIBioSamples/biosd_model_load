#!/bin/sh

# Deploys the command line binary. This doesn't do much, but it's needed with Bamboo.
# 

MYDIR=$(dirname "$0")

cd "$MYDIR"
vcmd='mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version --offline'
version=$($vcmd | grep -Ei '^[0-9]+[a-z,+,-,\.,_]')

cd "$MYDIR"/target

target="$1"

if [ "$target" == "" ]; then
  target=.
fi;

echo
echo 
echo _______________ Deploying the Command Line Binary to $target _________________

yes A| unzip biosd_loader_shell_*.zip -d "$target"
chmod -R ug=rwX,o=rX "$target/biosd_loader_shell_$version"

echo ______________________________________________________________________________
echo
echo
echo
