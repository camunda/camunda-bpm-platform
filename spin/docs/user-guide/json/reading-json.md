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

> **Closing the input stream**: Note that spin does not close the input stream. Users are requierd
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

## Configuring Json Import

Whenever the json data format is explicitly declared, i.e. by using `JSON(..., jsonTree())` 
or `S(..., jsonTree())`, configuration to the Jackson parser can be passed. This can be either done by using
a fluent API or by providing a map of configuration parameters.

### Examples:

Configuring the jackson parser to allow leading zeros for numbers works as follows in Java:

```java
import static org.camunda.spin.Spin.*;

SpinJsonNode json = S("{\"age\": 042}", jsonTree().allowNumericLeadingZeros(true));
```

The data format can also be configured using a map, which comes in handy in scripting environments. The following
example uses Javascript:

```javascript
var customer = S('{"age": 042}', jsonTree().config({allowNumericLeadingZeros: true}));
```

A more concise equivalent:

```javascript
var customer = JSON('{"age": 042}', {allowNumericLeadingZeros: true});
```

### Configuration Options:

The following explanations are supported:

<table>
  <tr>
    <th>Name</th>
    <th>Type</th>
  </tr>
  <tr>
    <td>allowComments</td>
    <td>Boolean</td>
  </tr>
  <tr>
    <td>allowUnquotedFieldNames</td>
    <td>Boolean</td>
  </tr>
  <tr>
    <td>allowSingleQuotes</td>
    <td>Boolean</td>
  </tr>
  <tr>
    <td>allowBackslashEscapingAnyCharacter</td>
    <td>Boolean</td>
  </tr>
  <tr>
    <td>allowNumericLeadingZeros</td>
    <td>Boolean</td>
  </tr>
  <tr>
    <td>allowNonNumericNumbers</td>
    <td>Boolean</td>
  </tr>
</table>

See the Jackson [JsonParser.Feature][jackson-parser-features] documentation for an explanation of the options.

[jackson-parser-features]: https://fasterxml.github.io/jackson-core/javadoc/2.3.0/com/fasterxml/jackson/core/JsonParser.Feature.html
