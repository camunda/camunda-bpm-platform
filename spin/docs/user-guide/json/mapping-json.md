# Mapping JSON

Spin can deserialize JSON to Java objects and serialize Java objects to JSON by integrating Jackson's mapping features into its fluent API.

## Mapping between Representations:

Assume we have a class `Customer` defined as follows:

```java
public class Customer {

  private String name;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
}
```

### Mapping JSON to Java:

We can map a JSON object `{"name" : "Kermit"}` to an instance of `Customer` as follows:

```java
import static org.camunda.spin.Spin.JSON;

Customer customer = JSON("{\"customer\": \"Kermit\"}").mapTo(Customer.class);
```

### Mapping Java to JSON:

We can map the `customer` back to JSON as follows:

```java
import static org.camunda.spin.Spin.JSON;

String json = JSON(customer).toString();
```

You can also map Java primitives like boolean or number values to the corresponding JSON values. For example, `JSON(true)` maps to the JSON constant of the boolean value `true`. However, note that String values are not converted but are interpreted as JSON input (see [Reading JSON][reading-json]). For example, `JSON("a String")` raises an exception because `"a String"` lacks additional escaped quotes and is no valid JSON. Nevertheless, a list of String values is properly converted to a JSON array of String values.

## Mapping to Generic Types:

Assume we have a list of customers that we would declare as `List<Customer>` in Java. For mapping a JSON array `[{"name" : "Kermit"}, {"name" : "Hugo"}]` to such a list, calling `mapTo(ArrayList.class)` is not sufficient as Jackson cannot tell of which type the array's elements are. This case can be handled by providing `mapTo` with a canonical type string, following Jackson's conventions:

```java
import static org.camunda.spin.Spin.JSON;

String json = "[{\"customer\": \"Kermit\"}, {\"customer\": \"Kermit\"}]"

List<Customer> customers = JSON("{\"customer\": \"Kermit\"}").mapTo("java.util.ArrayList<somepackage.Customer>");
```

## Mapping to Polymorphic Types:

Mapping JSON to Java objects is particularly tricky as JSON does not contain type information which is required to deserialize it to Java objects. In the above examples, we have explicitly told Spin to map the JSON object to a `Customer` object using the `mapTo` method. For nested JSON objects, Jackson is able to infer the desired deserialization type by inspecting the declared fields of the supplied class. However, this does not work for polymorphic types. Consider the following example, where the `Customer` has a reference to a `Car`. 

```java
public class Customer {

  private String name;
  private Car car;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public Car getCar() {
    return car;
  }
  
  public void setCar(Car car) {
    this.car = car;
  }
}
```

Assuming that `Car` is an interface with various implementations, such as `StationWagon` or `Van`, Jackson cannot tell which implementation to use based solely on the static structure of `Customer`. In these cases, Jackson relies on type information that is part of the JSON. See the [Jackson documentation][jackson-polymorphy] for the various options Jackson offers to configure type serialization and deserialization. You can configure these options in Spin as described in the [configuration section][configuring-json].


[jackson-polymorphy]: http://wiki.fasterxml.com/JacksonPolymorphicDeserialization
[reading-json]: reading-json.md
[configuring-json]: configuring-json.md