#camunda commons Testing

This project provides utility classes for Testing that are used in several Camunda projects

## Usage of the `ProcessEngineLoggingRule` class

1. Add a public field to the test class with type `ProcessEngineLoggingRule`
  * Optionally, provide the logger names of the loggers to watch for through `ProcessEngineLoggingRule#watch`
  * Optionally, provide the log level to watch for through `ProcessEngineLoggingRule#level`
2. Annotate the field with the `@Rule` annotation. E.g.:
```java
@Rule
public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();
```
3. Optionally, annotate the desired test cases with the `@WatchLogger` annotation. E.g.:
```java
@Test
@WatchLogger(loggerNames = {LIST_OF_LOGGER_NAMES}, level = "LOG_LEVEL")
public void testOverrideWithAnnotation() {

}
```
