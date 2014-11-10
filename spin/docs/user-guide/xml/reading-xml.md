# Reading XML

The XML datatype supports reading XML from Strings or input streams.

## Reading XML from a String:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinXmlElement xml = S("<order />", xml());
```

The second paramter `xml()` hints Spin to use the XML data format for parsing the XML.

Alternatively you can directly use the `XML(...)` function:

```java
import static org.camunda.spin.Spin.*;

SpinXmlElement xml = XML("<order />");
```

## Reading XML from a Reader:

Spin also supports reading XML directly from a `java.io.Reader`:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinXmlElement xml = S(reader, xml());
```

The `XML(...)` method also supports readers. The following example shows how to read the XML from a file (error handling ommitted):

```java
import static org.camunda.spin.Spin.*;

FileInputStream fis = new FileInputStream("/tmp/incomingOrder.xml");
InputStreamReader reader = new InputStreamReader(fis, "utf-8");
SpinXmlElement xml = XML(reader);
```

## Reading XML using a Script Language

XML can be read from script languages in the same way as from Java. Since script languages use dynamic typing, you do not need to hint the data format but you can use autodetection. The following example demonstrates how to read XML in JavaScript:

```javascript
var orderId = S('<order id="1231" />').attr('id');
```
