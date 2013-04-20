#!/usr/bin/env bash

#CLASSPATH=./scala_squib.jar scala -optimise -deprecation -savecompiled HouseOfMirrors.scala
scala -deprecation -savecompiled HouseOfMirrors.scala
#scala -optimise -deprecation -savecompiled HouseOfMirrors.scala

# I tried this temporarily when I didn't use Double Buffering. There's this old bug in AWT : 
# http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4833528
# CLASSPATH=./scala_squib.jar scala -Dsun.java2d.pmoffscreen=false -savecompiled HouseOfMirrors.scala
