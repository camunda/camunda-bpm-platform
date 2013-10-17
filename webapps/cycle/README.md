# camunda Cycle

The BPMN 2.0 roundtrip tool.


## Development Setup

You need:

* Maven
* Java (JDK 6 prefered to run test suite)


## Testing

To run all tests in the test suite execute

```
> cd cycle
> mvn clean test
```


## Running the Application

On the commandline execute

```
> cd cycle
> mvn clean tomcat:run -Ptomcat
```

This boots up an embedded tomcat on [http://localhost:8080/camunda-cycle/](http://localhost:8080/camunda-cycle/).


## Package application

```
> mvn clean install
```

Creates `.war` files for all target platforms (Glassfish, JBoss, Tomcat).

Have a look into the `cycle-(glassfish|jboss|tomcat)` folders to learn more.