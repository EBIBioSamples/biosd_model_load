#!/bin/sh

#�Deploys the command line binary on the EBI's NFS.
#�

MYDIR=$(dirname "$0")
cd "$MYDIR"/target

yes A| unzip biosd_loader_shell_*.zip -d /ebi/microarray/home/biosamples-dev/loader_relational/dev

