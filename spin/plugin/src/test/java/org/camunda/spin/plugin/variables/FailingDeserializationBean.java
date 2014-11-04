package org.camunda.spin.plugin.variables;

public class FailingDeserializationBean extends JsonSerializable {

  public FailingDeserializationBean() {
    throw new RuntimeException("I am failing");
  }

  public FailingDeserializationBean(String stringProperty, int intProperty, boolean booleanProperty) {
    super(stringProperty, intProperty, booleanProperty);
  }
}
