# Querying XML

The XML datatype supports querying with the XPath 1.0 query language.

## Querying an element

```java
import static org.camunda.spin.Spin.XML;

String xml = "<root><child id=\"child\"><a id=\"a\"/><a id=\"b\"/></child></root>";

SpinXmlTreeElement child = XML(xml).xPath("/root/child").element();
```

## Querying an element list

```java
import static org.camunda.spin.Spin.XML;

String xml = "<root><child id=\"child\"><a id=\"a\"/><a id=\"b\"/></child></root>";

SpinList<SpinXmlTreeElement> childs = XML(xml).xPath("/root/child/a").elementList();
```

## Querying an attribute

```java
import static org.camunda.spin.Spin.XML;

String xml = "<root><child id=\"child\"><a id=\"a\"/><a id=\"b\"/></child></root>";

SpinXmlTreeAttribute attribute = XML(xml).xPath("/root/child/@id").attribute();
```

## Querying an attribute list

```java
import static org.camunda.spin.Spin.XML;

String xml = "<root><child id=\"child\"><a id=\"a\"/><a id=\"b\"/></child></root>";

SpinList<SpinXmlTreeAttribute> attributes = XML(xml).xPath("/root/child/a/@id").attributeList();
```

## Querying a String

```java
import static org.camunda.spin.Spin.XML;

String xml = "<root><child id=\"child\"><a id=\"a\"/><a id=\"b\"/></child></root>";

String value = XML(xml).xPath("string(/root/child/@id)").string();
```

## Querying a Number

```java
import static org.camunda.spin.Spin.XML;

String xml = "<root><child id=\"child\"><a id=\"a\"/><a id=\"b\"/></child></root>";

Double count = XML(xml).xPath("count(/root/child/a)").number();
```

## Querying a Boolean

```java
import static org.camunda.spin.Spin.XML;

String xml = "<root><child id=\"child\"><a id=\"a\"/><a id=\"b\"/></child></root>";

Boolean exists = XML(xml).xPath("boolean(/root/child)").bool();
```
