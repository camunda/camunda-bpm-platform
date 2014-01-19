# The Process Engine Performance Test Suite

This testsuite allows running different kinds of performance tests against the process engine:

* Benchmarks
* Sql Statement Log (Database communication Profile)

> **Design Rationale**: This testsuite does not try to produce absolute numbers. The goal is not to produce numbers that show "how fast the process engine is". On the contrary, the idea is to produce relative numbers that can be compared over time. The benchmarks allow us to get a sense of whether a certain change to the codebase made the process engine faster or slower compared to the numbers we were getting before. Other performance tests like the Sql Statement Log are meant to serve as a tool for gaining insight into the inner workings of the perocess engine and may be used for tracking down the source of performance degradations or for finding potential for optimization.

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

## The Sql Statement Log

The Sql Statement Log allows you to gain insight into the process engine's communication profile with the database. This is interesting since the database communication is the main source of performance bottlenecks. It also provides the most potential for optimizations.

### Running the Sql Statement Log

The sql-statementlog will typically run each performance test once and on a single thread. In order to run it, use the `sql-statementlog` profile:

```Shell
mvn clean install -Psql-statementlog,h2 
```

### Inpecting the Sql Statement Log Results

Running the Sql Statement Log will produce the following folders in the `target/` folder of the project:

* `reports/` - containing an aggregated report for all tests run in both HTML and JSON format.
* `results/` - containing the results of the individual test runs in raw JSON format.

The Html Report gives you an aggregated overview over the INSERT / UPDATE / DELETE / SELECT statements executed by each test:

![Statement log Screenshot][1]

The raw JSON result files allow you to inspect the database communication between the process engine and the database on a fine grained level:

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

[1]: docs/sql-statement-log-report.png
