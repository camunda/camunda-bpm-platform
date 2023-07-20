# The Process Engine Performance Test Suite

This testsuite allows running different kinds of performance tests against the process engine.

**Table of Contents:**

* [The Benchmark](#benchmark)
* [The Sql Statement Log](#sql-statement-log)
* [The Activity Log](#activity-log)
* [Configuration](#configuration)
   1. [Database](#configuration-database)
   2. [History](#configuration-history)

> **Design Rationale**: This testsuite does not try to produce absolute numbers. The goal is not to produce numbers that show "how fast the process engine is". On the contrary, the idea is to produce relative numbers that can be compared over time. The benchmarks allow us to get a sense of whether a certain change to the codebase made the process engine faster or slower compared to the numbers we were getting before. Other performance tests like the Sql Statement Log are meant to serve as a tool for gaining insight into the inner workings of the process engine and may be used for tracking down the source of performance degradations or for finding potential for optimization.

<a name="benchmark"></a>
## The Benchmark

The benchmark runs the performance testsuite in multiple passes. Each pass will perform a configurable number of iterations on a certain number of threads. The first pass will use one thread, the second one two threads, the third one three theads and so forth. The benchmark gives some relative numbers as to how long it takes to run a certain process in a number of iterations with a given amount of threads. It will also show to which extent adding more threads will influence the performance numbers (scale up).

### Running the Benchmark

In order to run a benchmark, you need to select the `benchmark` profile:

```Shell
mvn clean install -Pbenchmark,h2 -DnumberOfThreads=4 -DnumberOfRuns=10000
```
### Inspecting the Benchmark Results

Running the Sql Statement Log will produce the following folders in the `target/` folder of the project:

* `reports/` - containing an aggregated report for all tests run in both HTML and JSON format.
* `results/` - containing the results of the individual test runs in raw JSON format.

The Html Report gives you an aggregated overview of the execution times in milliseconds:

![Benchmark Screenshot][1]

The raw JSON result files are located in the `target/results/` folder and provide the numbers collected for each pass.

```json
{
  "testName" : "SequencePerformanceTest.asyncSequence5Steps",
  "configuration" : {
    "numberOfThreads" : 4,
    "numberOfRuns" : 500,
    "testWatchers" : null
  },
  "passResults" : [ {
    "duration" : 650,
    "numberOfThreads" : 1,
    "stepResults" : [ ]
  }, {
    "duration" : 401,
    "numberOfThreads" : 2,
    "stepResults" : [ ]
  }, ...
}
```
<a name="longterm-results" />
### Collect longterm results

You could collect your benchmarks over a longer time if you pass the absolute filename
of a csv file as a commandline parameter. Here you are able to compare different runs in different
configurations over a long period. If you choose a file outside of your target directory, it won't be deleted
for the next run.

```Shell
mvn clean install -Pbenchmark,h2 \ 
                  -DlongTermBenchmarkResultFile=C:\\Arbeit\\camunda\\performanceTests\\longtermResults.csv
```

The results file may look like this:

![LongTermBenchmarkResult Screenshot][3]

This feature works only in the benchmark profile.  

<a name="sql-statement-log"></a>
## The Sql Statement Log

The Sql Statement Log allows you to gain insight into the process engine's communication profile with the database. This is interesting since the database communication is the main source of performance bottlenecks. It also provides the most potential for optimizations.

### Running the Sql Statement Log

The sql-statementlog will typically run each performance test once and on a single thread. In order to run it, use the `sql-statementlog` profile:

```Shell
mvn clean install -Psql-statementlog,h2 
```

### Inspecting the Sql Statement Log Results

Running the Sql Statement Log will produce the following folders in the `target/` folder of the project:

* `reports/` - containing an aggregated report for all tests run in both HTML and JSON format.
* `results/` - containing the results of the individual test runs in raw JSON format.

The Html Report gives you an aggregated overview over the INSERT / UPDATE / DELETE / SELECT statements executed by each test:

![Statement log Screenshot][2]

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


<a name="activity-log"></a>
## The Activity Log

The Activity Log allows you to gain insight into the process engine's job execution.

### Running the Activity Log

The to enable the activity log set the `watchActivities` properties to the activity ids to log. To create a reports for this activities use the `activity-count` profile.
**Note**: Currently the activity count report only supports one test process to be execute, otherwise the report messed up.

```Shell
mvn clean install -P activity-count,h2 -DwatchActivities=start,timer,end -Dtest=AsyncStartAndTimerPerformanceTest
```

### Inspecting the Activity Log Results

Running the Activity Log will produce the following folders in the `target/` folder of the project:

* `reports/` - containing an aggregated report for all recorded activities in HTML, JSON and CSV format.
* `results/` - containing the results of the individual test runs in raw JSON format.

The HTML report gives you an aggregated overview over the start time, end time and average duration of every activity cumulated over the execution time:

![Activity log Screenshot][4]

The raw JSON result files allow you to inspect the activity execution on a fine grained level:

```json
{
  "testName" : "AsyncStartAndTimerPerformanceTest.test",
  "configuration" : {
    "numberOfThreads" : 1,
    "numberOfRuns" : 100,
    "databaseName" : "org.postgresql.Driver",
    "testWatchers" : "",
    "historyLevel" : "full",
    "watchActivities" : [ "start", "timer", "end" ],
    "startTime" : 1430124594459,
    "platform" : "camunda BPM"
  },
  "passResults" : [ {
    "duration" : 15627,
    "numberOfThreads" : 1,
    "stepResults" : [ ],
    "activityResults" : {
      "27060" : [ {
        "activityInstanceId" : "start:27076",
        "activityId" : "start",
        "processInstanceId" : "27060",
        "startTime" : 1430124596918,
        "endTime" : 1430124596919,
        "duration" : 1
      }, {
        "activityInstanceId" : "timer:27081",
        "activityId" : "timer",
        "processInstanceId" : "27060",
        "startTime" : 1430124596925,
        "endTime" : 1430124609836,
        "duration" : 12911
      }, {
        "activityInstanceId" : "end:27789",
        "activityId" : "end",
        "processInstanceId" : "27060",
        "startTime" : 1430124609842,
        "endTime" : 1430124609842,
        "duration" : 0
      } ],
      ...
}
```


<a name="configuration" />
## Configuration

The process engine testsuie can be configured through the maven build.

<a name="configuration-database" />
### Selecting a database

Databases are selected using maven profiles:

```Shell
mvn clean install -Pbenchmark,h2
mvn clean install -Pbenchmark,mysql
mvn clean install -Pbenchmark,postgresql
mvn clean install -Pbenchmark,oracle
mvn clean install -Pbenchmark,db2
```

The connection properties can be set

* Through environment variables
* In a global maven settings.xml file
* Through command line arguments

Example for command line parameters:

```Shell
mvn clean install -Pbenchmark,mysql \
                  -Ddatabase.driver=com.mysql.jdbc.Driver \
                  -Ddatabase.url=jdbc:mysql://localhost:3306:camunda \
                  -Ddatabase.username=oscar \
                  -Ddatabase.password=s3cret \
```

<a name="configuration-history" />
### Selecting a history level

History levels can be selected using a maven profile:

```Shell
mvn clean install -Pbenchmark,mysql,history-level-none
mvn clean install -Pbenchmark,mysql,history-level-full
```

<a name="configuration-tests" />
### Selecting tests

A test or a group of tests can be selected using the properties `test.includes` and `test.excludes`:

```Shell
mvn clean install -Pbenchmark,mysql -Dtest.includes=dmn
```

Or use one of the predefined maven profiles:

```Shell
mvn clean install -Pbenchmark,mysql,testBpmn
mvn clean install -Pbenchmark,mysql,testDmn
```

[1]: docs/benchmark-report.png
[2]: docs/sql-statement-log-report.png
[3]: docs/longTermBenchmarkResults.png
[4]: docs/activity-log-report.png
