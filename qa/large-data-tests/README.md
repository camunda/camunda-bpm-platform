# The Large Data Test Suite

This testsuite checks if the engine and its database can cope with large data, e.g. the page size 
that Optimize uses to page through the engine data. 

Context: in the past we had several support cases where the page size caused problems
for some of the supported databases. To ensure that we get informed before the
issue show up at the users we have this tests in place.

To run the tests just execute the following command:
```bash
mvn clean test -Plarge-data-tests
```

## Running tests with the Maven Wrapper

With `mvnw`, from the root of the project,
run: `./mvnw clean test -f qa/large-data-tests/pom.xml -Plarge-data-tests,${database-id}`
where `${database-id}` is for example `h2`.

## Parameters to adjust

You can select which database to test against by combining the respective database profile
with the optimize tests (the database needs to run locally):
```bash
mvn clean test -Plarge-data-tests,mysql
```