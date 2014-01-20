#
# Lists all the SampleTab files in $dir. This has to be done by splitting the job into parallel 'find' invocations,
# based on the directories at first level. That's because we've a lot of files and our NFS is not exactly fast.
#
cd $(dirname "$0")

# TODO: different dirs for dev/test/prod?
dir=/ebi/microarray/ma-exp/biosamples

ct=1
for d in $(find $dir -mindepth 1 -maxdepth 1 -type d)
do
  find $d \
  -type d -name 'age' -prune -type d -name '*_log' -prune \
  -name '*.sampletab.toload.txt' -or -name 'sampletab.toload.txt' >_files_$ct.lst 2>_files_$ct.err &
  ct=$(($ct + 1))
done

echo "Parallel Sampletab search launched, please have a break while I rummage the file system..."
wait

# Now join all up
cat _files_*.lst >"$1".lst
cat _files_*.err >"$1".err
rm -f _files_*.*

echo Done.
