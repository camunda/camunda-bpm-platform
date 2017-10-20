To test the database upgrade you have to build the SQL scripts

```
cd camunda-bpm-platform/distro/sql-script
mvn clean install
```

Then you can execute the test for the current major version (e.g. 72) and database (e.g. postgres)

```
mvn clean install -Pupgrade,${CURRENT_MAJOR_VERSION},${DATABASE}
```
