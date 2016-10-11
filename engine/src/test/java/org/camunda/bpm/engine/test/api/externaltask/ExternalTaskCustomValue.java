package org.camunda.bpm.engine.test.api.externaltask;

import java.io.Serializable;

public class ExternalTaskCustomValue implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String testValue;

  public String getTestValue() {
    return testValue;
  }

  public void setTestValue(String testValue) {
    this.testValue = testValue;
  }

}
