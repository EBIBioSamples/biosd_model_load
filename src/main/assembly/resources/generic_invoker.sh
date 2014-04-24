#!/bin/sh

# This is used to invoke both the loader and the unloader. Do not call me directly. Use <loader/unloader>.sh instead
# 

# These are passed to the JVM. they're appended, so that you can predefine it from the shell
OPTS="$OPTS -Xms2G -Xmx4G -XX:PermSize=128m -XX:MaxPermSize=256m -XX:+UseConcMarkSweepGC"

# We always work with universal text encoding.
OPTS="$OPTS -Dfile.encoding=UTF-8"

# Monitoring with jconsole (end-user doesn't usually need this)
#OPTS="$OPTS 
# -Dcom.sun.management.jmxremote.port=5010
# -Dcom.sun.management.jmxremote.authenticate=false
# -Dcom.sun.management.jmxremote.ssl=false"
       
# Used for invoking a command in debug mode (end user doesn't usually need this)
#OPTS="$OPTS -Xdebug -Xnoagent"
#OPTS="$OPTS -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# The Database driver. You need to set this to your driver, in case you don't use one of the provided ones
# 
#JDBCPATH=/path/to/jdbc_driver.jar

# You shouldn't need to change the rest
#
###

#ensure that any files are group writable
umask 002

javaClass=$1
shift

cd "$(dirname $0)"
MYDIR="$(pwd)"

CP="$MYDIR:$MYDIR/biosd_loader_deps.jar"
if [ "$JDBCPATH" != "" ]; then
  CP="$CP:$JDBCPATH"
fi

JAVA_BIN=java
if [ "$JAVA_HOME" != "" ]; then
	JAVA_BIN="$JAVA_HOME/bin/java"
fi

# See here for an explaination about ${1+"$@"} :
# http://stackoverflow.com/questions/743454/space-in-java-command-line-arguments 
"$JAVA_BIN" $OPTS -cp $CP $javaClass ${1+"$@"}
EXCODE=$?

echo Java Finished. Quitting the Shell Too. >&2
echo 
exit $EXCODE
