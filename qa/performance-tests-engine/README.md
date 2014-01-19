# The Process Engine Performance Test Suite

This testsuite allows running different kinds of performance tests against the process engine:

* Benchmarks
* Database communitcation Profile (Sql Statement Log)

## Running a Benchmark

In order to run a benchmark, you need to select the `benchmark` profile:

```Shell
mvn clean install -Pbenchmark,h2 -DnumberOfThreads=4 -DnumberOfRuns=10000
```

The results are collected as JSON files in the `target/results/` folder:

```json
{
    "testName": "org.camunda.bpm.qa.performance.engine.bpmn.SequencePerformanceTest.asyncSequence15Steps",
    "configuration": {
        "numberOfThreads": 4,
        "numberOfRuns": 10000,
        "testWatchers": null
    },
    "duration": 23960,
    "stepResults": []
}
```

## Running the Sql Statement Log

The Sql Statement Log allows you to gain insight into the process engine's communication profile with the database. This is interesting since the database communication is the main source of performance bottlenecks. It also provides the most potential for optimizations.

The sql-statementlog will typically run each performance test once and on a single thread. In order to run it, use the `sql-statementlog` profile:

```Shell
mvn clean install -Psql-statementlog,h2 
```

This will produce JSON result files in the `target/results/` folder. The JSON files allow you to inspect the database communication between the process engine and the database:

```json
{
    "testName": "org.camunda.bpm.qa.performance.engine.bpmn.SequencePerformanceTest.asyncSequence1Step",
    "configuration": {
        "numberOfThreads": 1,
        "numberOfRuns": 1,
        "testWatchers": "org.camunda.bpm.qa.performance.engine.sql.statementlog.StatementLogPerfTestWatcher"
    },
    "duration": 20,
    "stepResults": [
        {
            "stepName": "StartProcessInstanceStep",
            "resultData": [
                {
                    "statementType": "SELECT_MAP",
                    "statement": "selectLatestProcessDefinitionByKey",
                    "durationMs": 1
                },
                {
                    "statementType": "INSERT",
                    "statement": "insertExecution",
                    "durationMs": 2
                }, ...
            ]
        },
        {
            "stepName": "SignalExecutionStep",
            "resultData": [
                {
                    "statementType": "SELECT_MAP",
                    "statement": "selectExecution",
                    "durationMs": 3
                },
                {
                    "statementType": "SELECT_MAP",
                    "statement": "selectProcessDefinitionById",
                    "durationMs": 1
                }, ...               
            ]
        }
    ]
}
```