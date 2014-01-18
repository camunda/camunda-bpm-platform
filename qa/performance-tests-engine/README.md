# The Process Engine Performance Test Suite

## Running the Testsuite

The performance test suite can be runs as part of the maven build:


```
mvn clean install -Pperformance-test,h2 -DnumberOfThreads=4 -DnumberOfRuns=10000
```