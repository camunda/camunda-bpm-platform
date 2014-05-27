#camunda commons Logging

This project provides a minimalistic base for writing loggers that produce consistent log and
exception messages.

## Usage

Define a logger class for a _logical component_ within your application:

```java
public class UserRepositoryLogger extends BaseLogger {

  public static UserRepositoryLogger LOG = createLogger(
    UserRepositoryLogger.class, "MYAPP", "com.myapp.user", "01"
  );

  public void userCreated(String userId) {
    logDebug("001", "Created user with id '{}'", userId);
  }

}
```

You can then import your logger class and use it:


```java
public class UserRepository {

  private final static UserRepositoryLogger LOG = UserRepositoryLogger.LOG;

  public void createUser(UserEntity user) {
    // save user...
    // log message
    LOG.userCreated(user.getId());
  }

}
```

This would produce output of the following form:

```
com.myapp.user - MYAPP-01001 Created user with id '992112'
```



## Log messages

A camunda log message looks like this:

```
PROJ-20023 The attribute with the name 'invalidId' was not found in the element with id 'elementId' returning an empty string.
```

Or in template form:

```
[PROJECT_CODE]-[COMPONENT_ID][MESSAGE_CODE] [MESSAGE]
```

The intended interpretation of the message template is as follows:

- `PROJECT_CODE`: a short identifier for indicating the name of the project. Example: `SPIN`.
- `COMPONENT_ID`: a unique, two-digit (numeric) identifier for a component within a project.
- `MESSAGE_CODE`: a unique, three-digit (numeric) identifier for this log message.
- `LOG_MESSAGE`: the meaningful log message of the logging event with detailed
  information (surrounded by single quotes) where appropriate for example
  `Attribute with name 'abc' not found`

## Log levels

| Level   | Description
|---------|-------------
| `ERROR` | Signals an error which is severe but may allow the application to continue. <br/>**Example:** `SPIN-10033 Unable to change attribute with name 'id' to value '#-test'`
| `WARN`  | Signals a warning which can interfere with the correct execution of the application. <br/>**Example:** `SPIN-20023 The attribute with the name 'invalidId' was not found in the element with id 'elementId' returning an empty string.`
| `INFO`  | Informs about the progress of the application. <br/>**Example:** `SPIN-30001 File with name 'existingData.xml' successful read and parsed`
| `DEBUG` | Information to help debugging an application. <br/>**Example:** `SPIN-41233 Found '12' child elements of element with id 'testElement'`

