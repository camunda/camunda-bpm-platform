# ...

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

* Spin public Api throws Runtime Exception only. `SpinException` is the root of the exception hierarchy tree.
* Interal Api / Spi throws mostly checked exceptions which are caught by the public Api layer and mapped to the appropriate runtime exception(s).
* Exceptions have unique id / error message in the same fashion as log messages.


[slf4j]: http://www.slf4j.org/
[logback]: http://logback.qos.ch/
