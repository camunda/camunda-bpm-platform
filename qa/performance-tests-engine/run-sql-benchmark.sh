#!/bin/sh
mvn clean install -P sql-statementlog,h2,history-level-audit #-Dtest=CallActivityPerformanceTest
