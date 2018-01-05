package org.camunda.bpm.integrationtest.functional.condition.bean;

import javax.inject.Named;

@Named
public class ConditionalBean {

  public boolean isApplicable() {
    return true;
  }

}
