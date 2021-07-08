Instance Migration Tests
========================

These modules test scenarios in which process instances are started before upgrading Camunda from a minor version to another minor version and completed after. The tests ensure that object instances persisted in the database tables by the engine in a previous minor version can be successfully processed by succeeding versions. The most common use case is migration of execution trees.

Executing Tests
---------------

Run `mvn clean install -Pinstance-migration,${database-id}` where `${database-id}` is for example `h2`.

### Running tests with the Maven Wrapper

With `mvnw`, from the root of the project, 
run: `./mvnw clean install -f qa/test-db-instance-migration/pom.xml -Pinstance-migration,${database-id}` 
where `${database-id}` is for example `h2`.

Project Structure
-----------------

* `test-fixture-72`: Creates the `7.2.0` database schema
* Any `test-fixture-7x`: Applies patch scripts for version `7.(x-1)`. Applies migration scripts from `7.(x-1)` to `7.x`. Starts process instances with engine version `7.x.0`
* `test-migration`: Executes test cases to assure that process instances started by any of the `test-fixture` modules can be completed with the current engine version