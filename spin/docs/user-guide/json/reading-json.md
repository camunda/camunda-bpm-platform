# Reading JSON

The JSON datatype supports reading JSON from Strings or Readers.

## Reading JSON from a String:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinJsonNode json = S("{\"customer\": \"Kermit\"}", jsonTree());
```

The second paramter `jsonTree()` hints Spin to use the Jackson tree parser for parsing the JSON.

Alternatively, you can directly use the `JSON(...)` function:

```java
import static org.camunda.spin.Spin.*;

SpinJsonNode json = JSON("{\"customer\": \"Kermit\"}");
```

String values that represent JSON primitive values can also be read. For example, `JSON("true")` returns a `SpinJsonNode` that represents the boolean value `true`.

## Reading JSON from a Reader:

Spin also supports reading JSON from an instance of `java.io.Reader`:

```java
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.DataFormats.*;

SpinJsonNode json = S(reader, jsonTree());
```

The `JSON(...)` method also supports readers. The following example shows how to read JSON from a file (error handling ommitted):

```java
import static org.camunda.spin.Spin.*;

FileInputStream fis = new FileInputStream("/tmp/customer.json");
InputStreamReader reader = new InputStreamReader(fis, "utf-8");
SpinJsonNode json = JSON(reader);
```

## Reading JSON using a Script Language

JSON can be read from script languages in the same way as from Java. Since script languages use dynamic typing, you do not need to hint the data format but you can use autodetection. The following example demonstrates how to read JSON in Javascript:

```javascript
var customer = S('{"customer": "Kermit"}');
```

## Reading JSON Properties

To fetch properties from the JSON tree you can use `.prop("name")`. This will return the property as SpinJsonNode and you can use this to get the value of the property as the following examples will demonstrate:

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

Spin allows us to use the `.fieldNames()` method to fetch the names of all child nodes and properties in a node. The following example shows you how to use `.fieldNames()` in Java and Javascript.

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

## Set JSON Properties

To set a property you can use the `.prop("name", object)` method. This allows you to set one of the following 5 simple types:

  * String - Example: `.prop("name", "Waldo")`
  * Integer - Example: `.prop("age", 42)`
  * Long - Example: `.prop("period", 4200000000)`
  * Float - Example: `.prop("price", 42.00)`
  * Boolean - Example: `.prop("active", true)`
  
or one of the 2 following container types:

  * Array - Could contain a number of simple and container types
  Example in Java:
  ```java
  import static org.camunda.spin.Spin.*;
  
  SpinJsonNode json = JSON("{\"customer\": \[\"Kermit\", \"Waldo\"\]}");
  ArrayList<Object> list = new ArrayList<Object>();
  list.add("new entry");
  list.add("new entry2");
  json.prop("new_array", list);
  ```
  
  Example in Javascript:
  ```javascript
  var json = S('{"customer": ["Kermit", "Waldo"]}');
  var list = ["new entry", "new entry2"];
  json.prop("new_array", list);    
  ```
  
  * Object - Could contain a number of simple and container types
  Example in Java:
  ```java
  import static org.camunda.spin.Spin.*;
    
  SpinJsonNode json = JSON("{\"customer\": \[\"Kermit\", \"Waldo\"\]}");
  Map<String, Object> object = new HashMap<String, Object>();
  object.put("new_entry", 42);
  object.put("new_entry2", "Yeah!");
  json.prop("new_object", object);
  ```
    
  Example in Javascript:
  ```javascript
  var json = S('{"customer": ["Kermit", "Waldo"]}');
  var object = {
    "new_entry": 1, 
    "new_entry2": "Yeah!"
  };
  json.prop("new_array", object);    
  ```

## Remove JSON Properties

There are 2 ways to remove properties from a JSON object.
 
  * `.deleteProp("name")` - Removes a property with given name.
  * `.deleteProp(<List of names>)` - Removes one or more properties with given names.
  
For more Details see the following Examples for Javascript and Java.

Java:
```java
import static org.camunda.spin.Spin.*;
    
SpinJsonNode json = JSON("{\"customer\": \"Kermit\", \"language\": \"en\"}");
List<String> listOfNames = new ArrayList<String>();
listOfNames.add("customer");
listOfNames.add("language");

// removes only the customer property
json.deleteProp("customer");

// removes customer and language
json.deleteProp(list);
```

Javascript:
```javascript
var json = S('{"customer": ["Kermit", "Waldo"], "language": "en"}');
var list = ["customer", "en"];

// removes only the customer property
json.deleteProp("customer");

// removes customer and language
json.deleteProp(list);
```

## Work with JSON Arrays

JSON arrays represent a list of objects. Spin offers the following methods to manipulate this list:

  * `.indexOf(<Object>)` - Fetches the index of the FIRST occurrence of the searched object.
  * `.lastIndexOf(<Object>)` - Fetches the index of the LAST occurrence of the searched object.
  * `.append(<Object>)` - Appends an object to the end of the list.
  * `.insertAt(<Index>, <Object>)` - Appends an object at the specific index of the list.
  * `.insertBefore(<Search object>, <Object>)` - Inserts an object before the FIRST occurrence of another object.
  * `.insertAfter(<Search object>, <Object>)` - Inserts an object after the FIRST occurrence of another object.
  * `.remove(<Object>)` - Removes the FIRST occurrence of the object.
  * `.removeLast(<Object>)` - Removes the LAST occurrence of the object.
  * `.removeAt(Index)` - Removes the list entry at the specified index.
  
These methods allow us to work with JSON arrays in a fast way. To show this, we will use the following JSON Object as an example:

```json
{
  "test-array" : [
    "testdata1",
    "testdata2",
    1,
    2,
    true,
    1,
    false,
    1
  ]
}
```

So let's see how we can manipulate this list in some examples.

### Example 1 - Get the index of testdata2 and the last occurrence of '1':

```java
import static org.camunda.spin.Spin.*;

SpinJsonNode json = JSON("{\"test-array\" : [\"testdata1\",\"testdata2\",1,2,true,1,false,1]}");
SpinJsonNode list = json.prop("test-array");

Integer i = list.indexOf("testdata2"); // Should be '1'
Integer j = list.lastIndexOf(1); // Should be '7'
```

```javascript
var json = S('{"test-array" : ["testdata1","testdata2",1,2,true,1,false,1]}');
var list = json.prop("test-array");

var i = list.indexOf("testdata2"); // should be 1
var j = list.lastIndexOf(1); // Should be '7'
```

### Example 2 - Add and Remove data the the list:

```java
import static org.camunda.spin.Spin.*;

SpinJsonNode json = JSON("{\"test-array\" : [\"testdata1\",\"testdata2\",1,2,true,1,false,1]}");
SpinJsonNode list = json.prop("test-array");

list.append("test2"); // at the end of the list there should now be "test2"
list.remove("test2"); // Aaaand now, it is gone ;)

list.insertAt(1, "test3"); // test3 should now be inserted before testdata2
list.removeAt(1, "test3"); // Aaaand now, it is gone ;)

list.insertBefore(true, "test4"); // now there should be test4 on index 4
list.insertAfter(true, 5); // So now 5 is on index 6
```

```javascript
var json = S('{"test-array" : ["testdata1","testdata2",1,2,true,1,false,1]}');
var list = json.prop("test-array");

list.append("test2"); // at the end of the list there should now be "test2"
list.remove("test2"); // Aaaand now, it is gone ;)

list.insertAt(1, "test3"); // test3 should now be inserted before testdata2
list.removeAt(1, "test3"); // Aaaand now, it is gone ;)

list.insertBefore(true, "test4"); // now there should be test4 on index 4
list.insertAfter(true, 5); // So now 5 is on index 6
```

[jackson-parser-features]: https://fasterxml.github.io/jackson-core/javadoc/2.4/com/fasterxml/jackson/core/JsonParser.Feature.html
