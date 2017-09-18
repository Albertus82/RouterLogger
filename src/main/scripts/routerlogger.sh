#!/bin/sh
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
if [ "$1" = "" ]
  then if [ "$JAVA_HOME" != "" ]
  then nice "$JAVA_HOME/bin/java" -DSWT_GTK3=0 -Xms8m -Xmx64m -classpath "$PRGDIR/routerlogger.jar:$PRGDIR/dropins/*:$PRGDIR/lib/*" it.albertus.router.RouterLogger
  else nice java -DSWT_GTK3=0 -Xms8m -Xmx64m -classpath "$PRGDIR/routerlogger.jar:$PRGDIR/dropins/*:$PRGDIR/lib/*" it.albertus.router.RouterLogger
  fi
else
  if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms4m -Xmx16m -classpath "$PRGDIR/routerlogger.jar:$PRGDIR/dropins/*:$PRGDIR/lib/*" it.albertus.router.RouterLogger $1 $2
  else java -Xms4m -Xmx16m -classpath "$PRGDIR/routerlogger.jar:$PRGDIR/dropins/*:$PRGDIR/lib/*" it.albertus.router.RouterLogger $1 $2
  fi
fi
