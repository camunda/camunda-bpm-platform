To test the database upgrade you have to build the SQL scripts

```
cd camunda-bpm-platform/distro/sql-script
mvn clean install
```

Then you can execute the test by providing a database (e.g. postgres):

```
mvn clean install -Pupgrade-db,${DATABASE}
```

### Running tests with the Maven Wrapper

With `mvnw`, from the root of the project,
run: `./mvnw clean install -f qa/test-db-upgrade/pom.xml -Pupgrade-db,${database-id}`
where `${database-id}` is for example `h2`.