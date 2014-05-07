# How to contribute

We would love you to contribute to this project. You can do so in various ways.
The following documentation should help you to get started and explain main
concepts of the project architecture.

## Logging & Exception Handling

### Logging

camunda spin uses [slf4j][] as logging library. This allows the user of camunda
spin to decide which underlying logging framework should be used or to easily
integrate camunda spin into existing ones. For testing purpose camunda spin
itself uses [logback][] as logging backend.

All log messages of camunda spin follow the same convention. The message
contains several parts which are described in the following:

- `ComponentId`: a short identifier for the logging component like `SPIN`
- `MessageCode`: a unique five-digit number code which identifies the
  corresponding logging event for example `30023`
- `LogMessage`: the meaningful log message of the logging event with detailed
  information (surrounded by single quotes) where appropriate for example
  `Attribute with name 'abc' not found`

A possible log message would be:

```
SPIN-30023 The attribute with the name 'invalidId' was not found in the element with id 'elementId' returning an empty string.
```

Every log message/event is associated with a log level. This level defines the
first digit of the `MessageCode`. The following log levels are used
(sorted by severity):


| Level   | MessageCode | Description
|---------|:-----------:|-------------
| `FATAL` | 1xxxx       | The highest possible severity which signals an error which prevents the application from continuing. <br/>**Example:** `SPIN-10012 Unable to read file with the name 'notExisting.xml'`
| `ERROR` | 2xxxx       | Signals an error which is severe but may allow the application to continue. <br/>**Example:** `SPIN-20033 Unable to change attribute with name 'id' to value '#-test'`
| `WARN`  | 3xxxx       | Signals a warning which can interfere with the correct execution of the application. <br/>**Example:** `SPIN-30023 The attribute with the name 'invalidId' was not found in the element with id 'elementId' returning an empty string.`
| `INFO`  | 4xxxx       | Informs about the progress of the application. <br/>**Example:** `SPIN-40001 File with name 'existingData.xml' successful read and parsed`
| `DEBUG` | 5xxxx       | Information to help debugging an application. <br/>**Example:** `SPIN-51233 Found '12' child elements of element with id 'testElement'`
| `TRACE` | 6xxxx       | Only allowed during development. Should not be commited or at leased removed after completion of a feature. <br/>**Example:** `SPIN-60123 Recursively searching for child elements (current recursion depth '3')`

**TODO**: Describe code usage/patterns


### Exception handling

camunda spin uses two main exception classes: `SpinException` for internal
checked exceptions and `SpinRuntimeException` for unchecked exceptions. The
exception strategy is following some basic principals:

- Use checked exceptions (inheriting from `SpinException`) only if there exists
  a meaningful way for the caller to handle the exception. For example if an
  element of a data structure does not exist the caller could decide whether to
  return `null`, an empty string, empty list or something other appropriated.
  Unchecked exceptions (inheriting from `SpinRuntimeException`) signal faults of
  the application either coding bugs or situations which cannot be handled by the
  caller. Examples would be null pointer exceptions or configuration mistakes.
- **Important:** Never throw a checked exception in a public Api method. camunda
  spin is library which should be used as a fluent Api and also in small
  code snippets and expressions where an enforced error handling is not desirable.
- Always document all throw exceptions by a method and methods which are called.
- Only catch exception where they can be handled in an useful way.
- If an exception is catched, wrapped and rethrown do not log the exception and
  add the original exception to the new exception as cause (Do not use
  getMessage() or printStackTrace() on the original exception).
- Do not log checked exception as they are expected and should be handled
  without affect the application flow.
- Log unchecked exception but only if they are handled by the code (not
  rethrown).
- Never swallow an exception, handle it correctly or not at all.
- Use meaningful exception messages, unique exception codes and exception
  classes. Every type of exception should be grouped into its own exception
  subclass (either inherit `SpinException` or `SpinRuntimeException`). The
  exception message should be informative. Every exception thrown should
  have a unique identifier which associate it with a exception class,
  exception type and exception message.

As convention exception messages follow a similar structure as log messages:

- `ComponentId`: the static spin identifier `SPIN-` to simply filter error logs
  for example
- `ExceptionCode`: an unique six-digit identifier for every exception. The
  identifier has three parts and the pattern `TCCMMMM`. The first digit `T` is
  either `0` for all exception inheriting from `SpinRuntimeException` and `1` for
  all exceptions inheriting from `SpinException`. The following two digits `CC`
  represent the exception class. The last four digits `MMMM` identify the actual
  exception.
- The exception message should be expressive and optional contain data useful
  for the user.

An example exception message could be:

```
SPIN-0020052 The element has the wrong format to be handled as Xml element
```

**TODO:** Describe code usage/patterns

[slf4j]: http://www.slf4j.org/
[logback]: http://logback.qos.ch/
