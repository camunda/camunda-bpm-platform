# The Optimize API Test Suite

This testsuite checks if the engine and its database can cope with the page size 
that Optimize uses to page through the engine data. 

Context: in the past we had several support cases where the page size caused problems
for some of the supported databases. To ensure that we get informed before the
issue show up at the users we have this tests in place.

To run the tests just execute the following command:
```bash
mvn clean test -Poptimize-api-tests
```

## Parameters to adjust

You can adjust the Optimize page size that should be tested with the following command:
```bash
mvn clean test -Poptimize-api-tests -Doptimize.page.size=20
```

You can select which database to test against by combining the respective database profile
with the optimize tests (the database needs to run locally):
```bash
mvn clean test -Poptimize-api-tests,mysql
```