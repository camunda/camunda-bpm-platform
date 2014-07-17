# Reading Json

The Json datatype supports reading Json from Strings or input streams.

## Reading Json from a String:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinJsonNode json = S("{\"customer\": \"Kermit\"}", jsonTree());
```

The second paramter `jsonTree()` hints Spin to use the Jackson tree parser for parsing the json.

Alternatively you can directly use the `JSON(...)` function:

```java
import static org.camunda.spin.Spin.*;

SpinJsonNode json = JSON("{\"customer\": \"Kermit\"}");
```

## Reading Json from an InputStream:

Spin also supports reading Json directly from a `java.io.InputStream`:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinJsonNode json = S(inputStram, jsonTree());
```

> **Closing the input stream**: Note that spin does not close the input stream. Users are required
> to close the input stream after fully processing it with spin.

The `JSON(...)` method also supports input streams. The following example shows how to read Json
from a File (error handling ommitted):

```java
import static org.camunda.spin.Spin.*;

FileInputStream fis = new FileInputStream("/tmp/customer.json");
SpinJsonNode json = JSON(fis);

```

## Reading Json using a Script Language

Json can be read from script languages in the same way as from Java. Since script languages use
dynamic typing, you do not need to hint the data format but you can use auto detection. The
following example demonstrates how to read Json in Javascript:

```javascript
var customer = S('{"customer": "Kermit"}');
```

## Reading Json Properties

To fetch properties from the Json tree you can use `.prop("name")`. This will return the property as
SpinJsonNode and you can use this to get the value of the property as the following examples will
demonstrate:

in Java:
```java
import static org.camunda.spin.Spin.*;

SpinJsonNode json = JSON("{\"customer\": \"Kermit\"}");
SpinJsonNode customerProperty = json.prop("customer");
String customerName = customerProperty.value();
```

in Javascript:
```javascript
var json = S('{"customer": "Kermit"}');
var customerProperty = json.prop("customer");
var customerName = customerProperty.value();
```

### The different value types

With `.value()` you will fetch a String representation of the value. There are also:

  * `.numberValue()` - will fetch a number representation of the value or throws an exception if the value is not a number
  * `.boolValue()` - will fetch a boolean representation of the value or throws an exception if the value is not a bool

### Fetch array of data

You can also fetch a list of items if your property is an array of data.

in Java:
```java
import static org.camunda.spin.Spin.*;

SpinJsonNode json = JSON("{\"customer\": \[\"Kermit\", \"Waldo\"\]}");
SpinJsonNode customerProperty = json.prop("customer");
SpinList customers = customerProperty.elements();
SpinJsonNode customer = customers.get(0);
String customerName = customer.value();
```

in Javascript:
```javascript
var json = S('{"customer": ["Kermit", "Waldo"]}');
var customerProperty = json.prop("customer");
var customers = customerProperty.elements();
var customer = customers.get(0)
var customerName = customer.value();
```

### Fetch field names

Spin allows us to use the `.fieldNames()` method to fetch the names of all child nodes and properties in a node.
The following example shows you how to use `.fieldNames()` in Java and Javascript.

in Java:
```java
import static org.camunda.spin.Spin.*;

SpinJsonNode json = JSON("{\"customer\": \[\"Kermit\", \"Waldo\"\]}");
ArrayList fieldNames = json.fieldNames();
String fieldName1 = fieldNames.get(0)
```

in Javascript:
```javascript
var json = S('{"customer": ["Kermit", "Waldo"]}');
var fieldNames = json.fieldNames();
var fieldName1 = fieldNames.get(0)
```

