# Mapping XML

Spin can deserialize XML to JAXB annotated Java objects and serialize the annotated Java objects to XML by integrating mapping features into its fluent API.

## Mapping between Representations:

Assume we have a class `Customer` defined as follows:

```java
@XmlRootElement(name="customer", namespace="http://camunda.org/test")
public class Customer {

  private String name;
  
  @XmlElement(namespace="http://camunda.org/test")
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
}
```

### Mapping XML to Java:

We can map the following XML object
 
 ```xml 
<?xml version="1.0" encoding="UTF-8"?>
<customer xmlns="http://camunda.org/example">
  <name>Kermit</name>
</customer>
 ```
 
 to an instance of `Customer` in the following way:

```java
import static org.camunda.spin.Spin.XML;

String xmlInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><customer xmlns=\"http://camunda.org/example\"><name>Kermit</name></customer>";

Customer customer = XML(xmlInput).mapTo(Customer.class);
```

### Mapping Java to XML:

We can map the `customer` back to XML as follows:

```java
import static org.camunda.spin.Spin.XML;

String xml = XML(customer).toString();
```
