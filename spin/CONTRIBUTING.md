# ...

## Logging & Exception Handling

### Logging

* meaningful log messages with ids / codes
* slf4j + log4j? [jBoss Logging?](https://github.com/weld/core/blob/master/impl/src/main/java/org/jboss/weld/logging/BeanManagerLogger.java)

### Exception handling

* Spin public Api throws Runtime Exception only. `SpinException` is the root of the exception hierarchy tree.
* Interal Api / Spi throws mostly checked exceptions which are caught by the public Api layer and mapped to the appropriate runtime exception(s).
* Exceptions have unique id / error message in the same fashion as log messages.

