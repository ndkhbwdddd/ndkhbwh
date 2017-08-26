#!/bin/sh

# -----------------------------------------------------------------------------
# Start/Stop Script for the CALENDAR AppServer
#
# Environment Variable Prequisites
#
#   CALENDAR_HOME   May point at your CALENDAR appserver "build" directory.
#
#   CALENDAR_BASE   (Optional) Base directory for resolving dynamic portions
#                   of a CALENDAR installation.  If not present, resolves to
#                   the same directory that CALENDAR_HOME points to.
#
#   CALENDAR_OPTS   (Optional) Java runtime options used when the "start",
#                   or "run" command is executed.
#
#   CALENDAR_TMPDIR (Optional) Directory path location of temporary directory
#                   the JVM should use (java.io.tmpdir).  Defaults to
#                   $CALENDAR_BASE/temp.
#
#
#   JAVA_OPTS       (Optional) Java runtime options used when the "start",
#                   "stop", or "run" command is executed.
#
#   JPDA_TRANSPORT  (Optional) JPDA transport used when the "jpda start"
#                   command is executed. The default is "dt_socket".
#
#   JPDA_ADDRESS    (Optional) Java runtime options used when the "jpda start"
#                   command is executed. The default is 8000.
#
#   JPDA_SUSPEND    (Optional) Java runtime options used when the "jpda start"
#                   command is executed. Specifies whether JVM should suspend
#                   execution immediately after startup. Default is "n".
#
#   JPDA_OPTS       (Optional) Java runtime options used when the "jpda start"
#                   command is executed. If used, JPDA_TRANSPORT, JPDA_ADDRESS,
#                   and JPDA_SUSPEND are ignored. Thus, all required jpda
#                   options MUST be specified. The default is:
#
#                   -Xdebug -Xrunjdwp:transport=$JPDA_TRANSPORT,
#                       address=$JPDA_ADDRESS,server=y,suspend=$JPDA_SUSPEND
#
#
#   CALENDAR_PID    (Optional) Path of the file which should contains the pid
#                   of CALENDAR startup java process, when start (fork) is used
#
# $Id: calendar-appserver.sh 2016-11-28 17:24:19Z jim $
# -----------------------------------------------------------------------------
LC_ALL=en_US.UTF-8
# Business Server Port
export PORT_BUSINESS=8080

#Server Class for CALENDAR
_ServerClass=com.yitianyike.calendar.appserver.startup.Bootstrap

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
os400=false
darwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
Darwin*) darwin=true;;
esac

# resolve links - $0 may be a softlink
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

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set CALENDAR_HOME if not already set
[ -z "$CALENDAR_HOME" ] && CALENDAR_HOME=`cd "$PRGDIR/.." ; pwd`


# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
  # Bugzilla 37284 (reviewed).
  if $darwin; then
    if [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
      export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
    fi
  else
    JAVA_PATH=`which java 2>/dev/null`
    if [ "x$JAVA_PATH" != "x" ]; then
      JAVA_PATH=`dirname $JAVA_PATH 2>/dev/null`
      JRE_HOME=`dirname $JAVA_PATH 2>/dev/null`
    fi
    if [ "x$JRE_HOME" = "x" ]; then
      # XXX: Should we try other locations?
      if [ -x /usr/bin/java ]; then
        JRE_HOME=/usr
      fi
    fi
  fi
  if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
    echo "Neither the JAVA_HOME nor the JRE_HOME environment variable is defined"
    echo "At least one of these environment variable is needed to run this program"
    exit 1
  fi
fi
if [ -z "$JAVA_HOME" -a "$1" = "debug" ]; then
  echo "JAVA_HOME should point to a JDK in order to run in debug mode."
  exit 1
fi
if [ -z "$JRE_HOME" ]; then
  JRE_HOME="$JAVA_HOME"
fi

# Set standard commands for invoking Java.
  _RUNJAVA="$JRE_HOME"/bin/java

if [ -z "$CALENDAR_BASE" ] ; then
  CALENDAR_BASE="$CALENDAR_HOME"
fi

CALENDAR_PID="$CALENDAR_BASE"/pid

if [ -z "$CALENDAR_TMPDIR" ] ; then
  # Define the java.io.tmpdir to use for CALENDAR
  CALENDAR_TMPDIR="$CALENDAR_BASE"/temp
fi

# Bugzilla 37848: When no TTY is available, don't output to console
have_tty=0
if [ "`tty`" != "not a tty" ]; then
    have_tty=1
fi

# ----- Execute The Requested Command -----------------------------------------

# Bugzilla 37848: only output this if we have a TTY
if [ $have_tty -eq 1 ]; then
  echo "Using CALENDAR_BASE:   $CALENDAR_BASE"
  echo "Using CALENDAR_HOME:   $CALENDAR_HOME"
  echo "Using CALENDAR_TMPDIR: $CALENDAR_TMPDIR"
  echo "Using JAVA_HOME:       $JAVA_HOME"
fi

# Add dependency library to CLASSPATH
if [ -r "$CALENDAR_HOME"/lib ]; then
	for x in "$CALENDAR_HOME"/lib/*
	do
	    CLASSPATH="$CLASSPATH":$x 
	done
else
	echo "debug mode. loading lib from project"
	if [ -r "$CALENDAR_HOME"/../lib ]; then
		cd "$CALENDAR_HOME"/../
		CALENDAR_HOME=`pwd`
		CLASSPATH="$CALENDAR_HOME"/bin
		for x in "$CALENDAR_HOME"/lib/*
		do
		    CLASSPATH="$CLASSPATH":$x 
		done
	else
		echo "loading lib failed"
		exit 1
	fi
fi
CLASSPATH="$CLASSPATH":"$CALENDAR_HOME"/lib:"$CALENDAR_HOME"/conf
JAVA_OPTS=" -server -Xms1280m -Xmx1280m -Xmn384m -XX:SurvivorRatio=1 -XX:PermSize=40m -XX:MaxPermSize=40m -XX:+UseParNewGC  -Xss256k "
if [ "$1" = "run" ]; then

  shift
  "$_RUNJAVA" $JAVA_OPTS $CALENDAR_OPTS \
    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
    -Dcalendar.base="$CALENDAR_BASE" \
    -Dcalendar.home="$CALENDAR_HOME" \
    -Djava.io.tmpdir="$CALENDAR_TMPDIR" \
    "$_ServerClass" "$@" start

elif [ "$1" = "start" ] ; then
  
  if [ ! -z "$CALENDAR_PID" ]; then
    if [ -f "$CALENDAR_PID" ]; then
      if [ -s "$CALENDAR_PID" ]; then
        echo "Existing PID file found during start."
        if [ -r "$CALENDAR_PID" ]; then
          PID=`cat "$CALENDAR_PID"`
          ps -p $PID >/dev/null 2>&1
          if [ $? -eq 0 ] ; then
            echo "Hcps appears to still be running with PID $PID. Start aborted."
            exit 1
          else
            echo "Removing/clearing stale PID file."
            rm -f "$CALENDAR_PID" >/dev/null 2>&1
            if [ $? != 0 ]; then
              if [ -w "$CALENDAR_PID" ]; then
                cat /dev/null > "$CALENDAR_PID"
              else
                echo "Unable to remove or clear stale PID file. Start aborted."
                exit 1
              fi
            fi
          fi
        else
          echo "Unable to read PID file. Start aborted."
          exit 1
        fi
      else
        rm -f "$CALENDAR_PID" >/dev/null 2>&1
        if [ $? != 0 ]; then
          if [ ! -w "$CALENDAR_PID" ]; then
            echo "Unable to remove or write to empty PID file. Start aborted."
            exit 1
          fi
        fi
      fi
    fi
  fi
  

  shift
  touch "$CALENDAR_BASE"/logs/calendar-appserver.out
  "$_RUNJAVA" $JAVA_OPTS $CALENDAR_OPTS \
    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
    -Dcalendar.base="$CALENDAR_BASE" \
    -Dcalendar.home="$CALENDAR_HOME" \
    -Djava.io.tmpdir="$CALENDAR_TMPDIR" \
    "$_ServerClass" "$@" start \
    >> "$CALENDAR_BASE"/logs/calendar-appserver.out 2>&1 &

    if [ ! -z "$CALENDAR_PID" ]; then
      echo $! > $CALENDAR_PID
    fi
    

elif [ "$1" = "stop" ] ; then

  shift
  FORCE=1
  if [ "$1" = "-force" ]; then
    shift
    FORCE=1
  fi

#  "$_RUNJAVA" $JAVA_OPTS \
#    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
#    -Dcalendar.base="$CALENDAR_BASE" \
#    -Dcalendar.home="$CALENDAR_HOME" \
#    -Djava.io.tmpdir="$CALENDAR_TMPDIR" \
#    "$_ServerClass" "$@" stop

  if [ $FORCE -eq 1 ]; then
    #if [ ! -z "$CALENDAR_PID" ]; then
    if [ -e $CALENDAR_PID -a ! -z $CALENDAR_PID ];then  
       echo "Killing: `cat $CALENDAR_PID`"
       kill -9 `cat $CALENDAR_PID`
       RETVAL=$?  
       echo  
       [ $RETVAL = 0 ] && rm -f $CALENDAR_PID
    else
       echo "Kill failed: \$CALENDAR_PID not set or don't exist!"
    fi
  fi

elif [ "$1" = "hardware" ] ; then

    echo "===========Hardware Status==========="
    echo "CPU core number:        `grep processor /proc/cpuinfo | wc -l` "
    echo `grep MemTotal /proc/meminfo`
    echo "TCP Connections(${PORT_BUSINESS}):        `netstat -an |grep tcp |grep $PORT_BUSINESS |wc -l`"

else

  echo "Usage: calendar-appserver.sh ( commands ... )"
  echo "commands:"
  echo "  run               Start calendar appserver in the current window"
  echo "  start             Start calendar appserver in a separate window"
  echo "  stop              Stop calendar appserver"
  echo "  stop -force       Stop calendar appserver (followed by kill -KILL)"
  echo "  hardware          Show hardware info"
  exit 1

fi