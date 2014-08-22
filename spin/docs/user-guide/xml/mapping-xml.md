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
 
 to an instance of `Customer` as follows:

```java
import static org.camunda.spin.Spin.XML;

Customer customer = XML("<?xml version=\"1.0\" encoding=\"UTF-8\"?><customer xmlns=\"http://camunda.org/example\"><name>Kermit</name></customer>").mapTo(Customer.class);
```

### Mapping Java to XML:

We can map the `customer` back to XML as follows:

```java
import static org.camunda.spin.Spin.XML;

String xml = XML(customer).toString();
```

### Configuring the mapper:

The mapper allows us to append some useful configuration parameters to the JAXB marshaller/unmarshaller. These parameters
can be configured as a map or simply as a key/value combination. With this you can set one of the following properties:

  * properties - A map which contains properties for the JAXB marshaller/unmarshaller
  * schema - contains the Schema for the JAXB Validation (The validation will be disabled if this is set to null)
  * eventHandler - holds the ValidationEventHandler for the marshaller/unmarshaller to fetch all thrown events (A default handler will be used if this is set to null)
  
An example of a configuration (as a key/value combination) could look like this:

```java
  SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
  Schema schema = sf.newSchema(new File("your_schema.xsd");
  
  XmlDomDataFormat dataFormat = new XmlDomDataFormat()
      .mapper()
      .config("schema", schema)
      .done();

  String xml = XML(customer, dataFormat).toString();
```

This would add `your_schema.xsd` to the marshaller and also activate the validation for this conversion.
