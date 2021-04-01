This test suite tests the engine of the last version with the current database schema.
This guarantees that an old engine can execute on a newer database schema,
which is needed for rolling upgrades.

The test of the old engine with the new database schema,
needs the database SQL scripts. These script
should been build before.

```
cd camunda-bpm-platform/distro/sql-script
mvn clean install
```

The test jar which contains the tests of the old engine
will be integrated via mvn dependency.

The test suite is executed like this:

```
mvn clean install -Pold-engine,${DATABASE}
```

### Running tests with the Maven Wrapper

With `mvnw`, from the root of the project,
run: `./mvnw clean install -f qa/test-old-engine/pom.xml -Pold-engine,${database-id}`
where `${database-id}` is for example `h2`.