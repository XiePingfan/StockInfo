#!/bin/sh
export JAVA_HOME=/home/tomcat/jdk1.7.0_60
export JRE_HOME=${JAVA_HOME}/jre
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib:${JAVA_HOME}/lib/tool.jar:${JAVA_HOME}/lib/rt.jar:../lib/mysql-connector-java-5.1.38-bin.jar:../lib/log4j-1.2.17.jar:../lib/mybatis-3.4.2.jar:../lib/gson-2.2.4.jar:
export PATH=${JAVA_HOME}/bin:$PATH
cd /home/mysql/StockInfo/bin
date > btime2.log
/home/tomcat/jdk1.7.0_60/bin/java org.tokenring.analysis.AnalysisParser >b.log 2>&1 &

