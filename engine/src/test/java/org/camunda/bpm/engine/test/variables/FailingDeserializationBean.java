package org.camunda.bpm.engine.test.variables;

public class FailingDeserializationBean extends SimpleBean {

  public FailingDeserializationBean() {
    throw new RuntimeException("I am failing");
  }

  public FailingDeserializationBean(String stringProperty, int intProperty, boolean booleanProperty) {
    super(stringProperty, intProperty, booleanProperty);
  }
}
