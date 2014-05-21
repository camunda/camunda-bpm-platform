# Manipulating XML

The Xml data type supports manipulating Xml attributes and child elements..

## Reading attributes from Xml

```java
import static org.camunda.spin.Spin.XML;

SpinXmlDomAttribute attribute = XML("<order id=\"order1\" />").attr("id");
String id = XML("<order id=\"order1\" />").attr("id").stringValue();
```

The `attr` method returns a wrapper of the Xml attribute and with `stringValue` the value of the
attribute can be accessed.

If you want to access an attribute in another namespace you have to use the `attrNs` method.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\" id=\"order1\" cam:name=\"order1\" />";

SpinXmlDomAttribute attribute = XML(xml).attrNs("http://camunda.org/example", "name");
```

You can also get a collection of all attributes under the default or a specific namespace.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\" id=\"order1\" cam:name=\"order1\" />";

// All attributes under the default namespace
SpinCollection<SpinXmlDomAttribute> attributes = XML(xml).attrs();

// All attributes under a specific namespace
attributes = XML(xml).attrs("http://camunda.org/example");
```

Or you can directly get all attribute names instead.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\" id=\"order1\" cam:name=\"order1\" />";

// All attribute names under the default namespace
List<String> names = XML(xml).attrNames();

// All attribute names under a specific namespace
names = XML(xml).attrNames("http://camunda.org/example");
```

## Reading child elements from Xml

Besides attributes you can also get an unique or all child elements of a specific type. Optionally
can a namespace be passed to the methods as first parameter.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\">" +
      "<date/><cam:due/><item/><item/><cam:op/><cam:op/></order>";

SpinXmlDomElement date = XML(xml).childElement("date");
SpinXmlDomElement due = XML(xml).childElement("http://camunda.org/example", "due");

SpinCollection<SpinXmlDomElement> items = XML(xml).childElements("item");
SpinCollection<SpinXmlDomElement> ops = XML(xml).childElements("http://camunda.org/example", "ops");
```


## Manipulating Xml using a Script Language

Xml can be manipulated from scrip languages in the same was as from Java. Since script languages use
dynamic typing, you do not need to hint the data format but you can use auto detection. The
following example demonstrates how to access an attribute and a child element from Xml in Python:

```python
xml = """
<order id="1231">
  <item id="1"/>
</order>
"""

order_id = S(xml).attr('id').stringValue()
assert order_id == "1231"

item_id = S(xml).childElement('item').attr('id').stringValue()
assert item_id == "1"
```
