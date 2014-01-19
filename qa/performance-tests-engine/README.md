# The Process Engine Performance Test Suite

## Running the Testsuite

The performance test suite runs as part of the maven build:

```
mvn clean install -Pperformance-test,h2 -DnumberOfThreads=4 -DnumberOfRuns=10000
```

The results are collected as JSON files in the `target/results/` folder.