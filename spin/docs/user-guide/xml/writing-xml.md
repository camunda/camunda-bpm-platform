# Writing Xml

The Xml datatype supports writing Xml to Strings, output streams or writers.

## Writing to a String:

```java
import static org.camunda.spin.Spin.XML;

SpinXmlTreeElement element = XML("<root id=\"test\"/>");

String xml = element.toString();


String value = element.attr("id").toString();
```

## Writing to a output stream:

```java
import static org.camunda.spin.Spin.XML;

SpinXmlTreeElement element = XML("<root id=\"test\"/>");

OutputStream ouputStream = element.toStream();

// write element again to stream
element.writeToStream(outputStream);


SpinXmlTreeAttribute attr = element.attr("id");
ouputStream = attr.toStream();

// write attribute value again to stream
attr.writeToStream(outputStream);
```

## Write to writer

```java
import static org.camunda.spin.Spin.XML;

SpinXmlTreeElement element = XML("<root id=\"test\"/>");

StringWriter writer = element.writeToWriter(new StringWriter());


SpinXmlTreeAttribute attr = element.attr("id");

writer = attr.writeToWriter(writer);
```
