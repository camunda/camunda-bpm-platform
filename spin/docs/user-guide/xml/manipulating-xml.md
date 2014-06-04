# Manipulating XML

The Xml data type supports manipulation of Xml attributes and child elements.


## Attributes


### Checking for attributes in Xml

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\" id=\"order1\" cam:name=\"name\" />";

boolean hasAttr = XML(xml).hasAttr("id");
assertTrue(hasAttr);

hasAttr = XML(xml).hasAttrNs("http://camunda.org/example", "id");
assertTrue(hasAttr);
```


### Reading attributes from Xml

```java
import static org.camunda.spin.Spin.XML;

SpinXmlDomAttribute attribute = XML("<order id=\"order1\" />").attr("id");
String id = XML("<order id=\"order1\" />").attr("id").value();
```

The `attr` method returns a wrapper of the Xml attribute and with `value` the value of the
attribute can be accessed.

If you want to access an attribute in another namespace you have to use the `attrNs` method.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\" id=\"order1\" cam:name=\"order1\" />";

SpinXmlDomAttribute attribute = XML(xml).attrNs("http://camunda.org/example", "name");
```

You can also get a collection of all attributes or only of a specific namespace.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\" id=\"order1\" cam:name=\"order1\" />";

// All attributes
SpinCollection<SpinXmlDomAttribute> attributes = XML(xml).attrs();

// All attributes of a specific namespace
attributes = XML(xml).attrs("http://camunda.org/example");
```

Or you can directly get all attribute names instead.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\" id=\"order1\" cam:name=\"order1\" />";

// All attribute names
List<String> names = XML(xml).attrNames();

// All attribute names of a specific namespace
names = XML(xml).attrNames("http://camunda.org/example");
```


### Writing attributes to Xml

It is possible to set a new attribute value directly from the element wrapper or on the attribute
wrapper.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order id=\"order1\" />";

XML(xml).attr("id", "newId");

SpinXmlDomAttribute attribute = XML(xml).attr("id");
attribute.value("newId");
```

You can also specify the namespace of the attribute to set.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\" id=\"order1\" cam:name=\"name\" />";

XML(xml).attrNs("http://camunda.org/example", "name", "newName");

SpinXmlDomAttribute attribute = XML(xml).attrNs("http://camunda.org/example", "name");
attribute.value("newName");
```


### Removing attributes from Xml

It is possible to remove a attribute from the element directly or to remove the attribute itself.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order id=\"order1\" />";

SpinXmlDomElement element = XML(xml).removeAttr("id");
assertFalse(element.hasAttr("id));

SpinXmlDomAttribute attribute = XML(xml).attr("id");
element = attribute.remove();
assertFalse(element.hasAttr("id));
```

You can also specify the namespace of the attribute to remove.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\" id=\"order1\" cam:name=\"name\" />";

SpinXmlDomElement element = XML(xml).removeAttrNs("http://camunda.org/example", "name");
assertFalse(element.hasAttrNs("http://camunda.org/example/", "name"));

SpinXmlDomAttribute attribute = XML(xml).attrNs("http://camunda.org/example", "name");
element = attribute.remove()
assertFalse(element.hasAttrNs("http://camunda.org/example", "name"));
```


## Child Elements


### Reading child elements from Xml

Besides attributes you can also get a unique or all child elements of a specific type. Optionally a namespace can be passed to the methods as first parameter.

```java
import static org.camunda.spin.Spin.XML;

String xml = "<order xmlns:cam=\"http://camunda.org/example\">" +
      "<date/><cam:due/><item/><item/><cam:op/><cam:op/></order>";

SpinXmlDomElement date = XML(xml).childElement("date");
SpinXmlDomElement due = XML(xml).childElement("http://camunda.org/example", "due");

SpinCollection<SpinXmlDomElement> items = XML(xml).childElements("item");
SpinCollection<SpinXmlDomElement> ops = XML(xml).childElements("http://camunda.org/example", "ops");
```


### Append child elements

The method `append` is used to append a single or multiple child elements to a Xml element.

```java
import static org.camunda.spin.Spin.XML;

SpinXmlTreeElement root = XML("<root/>");

SpinXmlTreeElement child1 = XML("<child/>");
SpinXmlTreeElement child2 = XML("<child/>");
SpinXmlTreeElement child3 = XML("<child/>");

root.append(child1, child2, child3);
```


### Remove child elements

To remove child elements from an Xml element the method `remove` is used. It accepts
a single or multiple child elements and removes them from the parent element.

```java
import static org.camunda.spin.Spin.XML;

SpinXmlTreeElement root = XML("<root><child/><child/><child/></root>");

root.remove(root.childElements("child"));
```


### Replace elements

To replace an element or a child element the methods `replace` and `replaceChild`
are used.

```java
import static org.camunda.spin.Spin.XML;

SpinXmlTreeElement root = XML("<root><date/><order/></root>");

SpinXmlTreeElement child1 = XML("<child/>");
root.replaceChild(root.childElement("date"), child1);

SpinXmlTreeElement child2 = XML("<child/>");
root.childElement("order").replace(child2);
```


## Manipulating Xml using a Script Language

Xml can be manipulated from script languages in the same was as from Java. Since script languages
use dynamic typing, you do not need to hint the data format but you can use auto detection. The
following example demonstrates how to access an attribute and a child element from Xml in Python:

```python
xml = """
<order id="1231">
  <item id="1"/>
</order>
"""

assert S(xml).hasAttr('id')

order_id = S(xml).attr('id').value()
assert order_id == '1231'

element = S(xml).attr('order', 'order1')
assert element.hasAttr('order')
assert element.Attr('order').value() == 'order1'

element.removeAttr('order')
assert not element.hasAttr('order')

item_id = S(xml).childElement('item').attr('id').value()
assert item_id == '1'
```
