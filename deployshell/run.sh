#!/bin/sh
date >> atime2.log
/home/mysql/StockInfo/bin/execjava.sh >a.log 2>&1 &
