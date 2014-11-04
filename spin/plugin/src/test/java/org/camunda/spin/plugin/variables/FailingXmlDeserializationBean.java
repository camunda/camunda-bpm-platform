package org.camunda.spin.plugin.variables;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FailingXmlDeserializationBean extends JsonSerializable {

  public FailingXmlDeserializationBean() {
    throw new RuntimeException("I am failing");
  }

  public FailingXmlDeserializationBean(String stringProperty, int intProperty, boolean booleanProperty) {
    super(stringProperty, intProperty, booleanProperty);
  }
}
