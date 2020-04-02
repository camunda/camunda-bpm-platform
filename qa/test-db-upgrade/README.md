To test the database upgrade you have to build the SQL scripts

```
cd camunda-bpm-platform/distro/sql-script
mvn clean install
```

Then you can execute the test by providing a database (e.g. postgres):

```
mvn clean install -Pupgrade-db,${DATABASE}
```
