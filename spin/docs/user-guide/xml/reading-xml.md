# Reading Xml

The xml datatype supports reading xml from Strings or input streams.

## Reading Xml from a String:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinXmlElement xml = S("<order />", xmlDom());
```

The second paramter `xmlDom()` hints Spin to use the Xml Dom parser for parsing the xml.

Alternatively you can directly use the `XML(...)` function:

```java
import static org.camunda.spin.Spin.*;

SpinXmlElement xml = XML("<order />");
```

## Reading Xml from an InputStream:

Spin also supports reading xml directly from a `java.io.InputStream`:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinXmlElement xml = S(inputStram, xmlDom());
```

> **Closing the input stream**: Note that spin does not close the input stream. Users are requierd
> to close the input stream after fully processing it with spin.

The `XML(...)` method also supports input streams. The following example shows how to read the xml
from a File (error handling ommitted):

```java
import static org.camunda.spin.Spin.*;

FileInputStream fis = new FileInputStream("/tmp/incomingOrder.xml");
SpinXmlElement xml = XML(fis);

```

## Reading XML using a Script Language

Xml can be read from script languages in the same way as from Java. Since script languages use
dynamic typing, you do not need to hint the data format but you can use auto detection. The
following example demonstrates how to read xml in Javascript:

```javascript
var orderId = S('<order id="1231" />').attr('id');
```
