package org.camunda.spin.plugin.variables;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FailingXmlSerializable extends XmlSerializable {

  public FailingXmlSerializable() {
  }

  public FailingXmlSerializable(String stringProperty, int intProperty, boolean booleanProperty) {
    super(stringProperty, intProperty, booleanProperty);
  }

  public String getStringProperty() {
    throw new RuntimeException("I am failing");
  }
}
