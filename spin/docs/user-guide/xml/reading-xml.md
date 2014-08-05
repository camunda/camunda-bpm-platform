# Reading XML

The XML datatype supports reading XML from Strings or input streams.

## Reading XML from a String:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinXmlElement xml = S("<order />", xmlDom());
```

The second paramter `xmlDom()` hints Spin to use the XML DOM parser for parsing the XML.

Alternatively you can directly use the `XML(...)` function:

```java
import static org.camunda.spin.Spin.*;

SpinXmlElement xml = XML("<order />");
```

## Reading XML from an InputStream:

Spin also supports reading XML directly from a `java.io.InputStream`:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinXmlElement xml = S(inputStram, xmlDom());
```

> **Closing the input stream**: Note that spin does not close the input stream. Users are required to close the input stream after fully processing it with Spin.

The `XML(...)` method also supports input streams. The following example shows how to read the XML from a file (error handling ommitted):

```java
import static org.camunda.spin.Spin.*;

FileInputStream fis = new FileInputStream("/tmp/incomingOrder.xml");
SpinXmlElement xml = XML(fis);

```

## Reading XML using a Script Language

XML can be read from script languages in the same way as from Java. Since script languages use dynamic typing, you do not need to hint the data format but you can use autodetection. The following example demonstrates how to read XML in JavaScript:

```javascript
var orderId = S('<order id="1231" />').attr('id');
```
