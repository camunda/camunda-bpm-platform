# Writing JSON

The JSON datatype supports writing JSON to Strings, output streams or writers.

## Writing to a String:

```java
import static org.camunda.spin.Spin.JSON;

SpinJsonNode jsonNode = JSON("{\"customer\": \"Kermit\"}");

String json = jsonNode.toString();
```

## Writing to an output stream:

```java
import static org.camunda.spin.Spin.JSON;

SpinJsonNode jsonNode = JSON("{\"customer\": \"Kermit\"}");

OutputStream ouputStream = jsonNode.toStream();
```

## Writing to a writer

```java
import static org.camunda.spin.Spin.JSON;

SpinJsonNode jsonNode = JSON("{\"customer\": \"Kermit\"}");

StringWriter writer = jsonNode.writeToWriter(new StringWriter());
```
