camunda-spin
============

<p>
  <a href="http://camunda.org/">Home</a> |
  <a href="http://docs.camunda.org/latest/api-references/spin/">Documentation</a> |
  <a href="http://camunda.org/community/forum.html">Forum</a> |
  <a href="https://app.camunda.com/jira/browse/CAM">Issues</a> |
  <a href="LICENSE">License</a> |
  <a href="CONTRIBUTING.md">Contribute</a>
</p>

Library for simple XML and JSON processing on the JVM (Java Virtual Machine), targeting Java and
JVM-based scripting languages such as Groovy, JRuby, Jython, Javascript and Java Expression
Language.

# Why Spin?

Spin is useful when you need to work with complex, text-based data formats (such as XML or JSON)
without mapping to Java Objects.

Spin provides a comprehensible fluent API for working with different data formats through 
lightweight wrapper objects:

Given the following XML document as `String` or `InputStream`:

```xml
<?xml version=“1.0“ encoding=“UTF-8“ ?>
<customers>
  <customer id="0001"><name>Jonny P</name></customer>
  <customer id="0002"><name>Bobby G</name></customer>
  <customer id="0003"><name>Mary T</name></customer>
</customers>
```

It can directly be worked on with Spin:

```java
import static org.camunda.spin.Spin.*;

// get id of first customer
XML( xmlInput ).childElements("customer")
  .get(0)
  .attr("id")
  .value();

// create new customer
SpinXmlTreeElement  kate = XML( "<customer />" )
  .attr("id", "0004")
  .append( XML("<name>Kate S</name>") );

// append to existing list
XML( xmlInput ).append(kate);

// query by Id:
XML( xmlInput ).xPath("/customers/customer[@id='0002']").element();
```


# FAQ

## Why should I use Spin instead of the Java DOM API?

Java provides a built-in Dom API along with a Parser. Spin provides a lightweight Wrapper around 
the Dom API which makes it easier to work with than the Dom API. You can access the underlying Dom object 
at any time by unwrapping it: 

```java
Element e = XML( xmlSource ).childElement("customer").unwrap();
```

## How can I contribute to Spin?

Check [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines about how to contribute.

## What license is used by Spin?

camunda Spin is licensed under the Apache 2.0 License.
