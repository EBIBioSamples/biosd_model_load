#!/bin/sh

MYDIR=$(dirname "$0")
cd "$MYDIR"/target

yes A| unzip biosd_loader_shell_*.zip -d /ebi/microarray/home/biosamples-dev/loader_relational/dev

