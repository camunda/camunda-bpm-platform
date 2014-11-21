# How to contribute

We would love you to contribute to this project. You can do so in various ways. The following 
documentation should help you to get started and explain the main concepts of the project architecture.

## Writing Docs

All docs are written in Markdown and are located in the [docs.camunda.org](https://github.com/camunda/docs.camunda.org)
repository.

### Markdown editing guidelines:

* Use pure Markdown (as opposed to embedded HTML) as much as possible.
* Markdown files should use a linewrap at 100 characters.
* All links must use the id-scheme and references must be placed at the end of the document.

## Logging & Exception Handling

This project uses [camunda commons logging][camunda-commons-logging].

### Exception handling

camunda Spin uses two main exception classes: `SpinException` for internal checked exceptions and
`SpinRuntimeException` for unchecked exceptions. The exception strategy follows some basic
principals:

- Use checked exceptions (inheriting from `SpinException`) only if there is a meaningful way for
  the caller to handle the exception. For example, if an element of a data structure does not exist,
  the caller could decide whether to return `null`, an empty string, an empty list or something otherwise
  appropriated. Unchecked exceptions (inheriting from `SpinRuntimeException`) signal faults of the
  application, either coding bugs or situations which cannot be handled by the caller. Examples would
  be null pointer exceptions or configuration mistakes.
- **Important:** Never throw a checked exception in a public API method. camunda Spin is a library
  which should be used as a fluent API and also in small code snippets and expressions where enforced 
  error handling is not desirable.
- Always document all throw exceptions by a method and methods which are called.
- Only catch exception where they can be handled in an useful way.
- If an exception is caught, wrapped and rethrown, do not log the exception and add the original
  exception to the new exception as cause (do not use getMessage() or printStackTrace() on the
  original exception).
- Do not log checked exception as they are expected and should be handled without affecting the
  application flow.
- Log unchecked exceptions, but only if they are handled by the code (not rethrown).
- Never swallow an exception, handle it correctly or not at all.
- Use meaningful exception messages, unique exception codes and exception classes. Every type of
  exception should be grouped into its own exception subclass (either inherit `SpinException` or
  `SpinRuntimeException`). The exception message should be informative. Every exception thrown 
  should have a unique identifier which associates it with a exception class, exception type and 
  exception message.

As a convention, exception messages follow a similar structure as log messages:

- `ComponentId`: the static Spin identifier `SPIN-` to simply filter error logs, for example
- `ExceptionCode`: a unique five-digit identifier for every exception. The
  identifier has two parts and the pattern `LLMMM`. The first two digits `LL`
  identify the logger and the last three digits `MMM` identify the exception
  message.
- The exception message should be expressive and can optionally contain data useful
  to the user.

An example exception message could be:

```
SPIN-12052 The element has the wrong format to be handled as XML element
```

**TODO:** Describe code usage/patterns

[slf4j]: http://www.slf4j.org/
[logback]: http://logback.qos.ch/
[camunda-commons-logging]: https://github.com/camunda/camunda-commons/tree/master/logging

