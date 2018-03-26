package org.camunda.bpm.integrationtest.functional.cdi.beans;

import javax.inject.Named;

@Named
public class RetryConfig {
  public RetryConfig() {
  }

  public String defaultConfig() {
    return "PT1M,PT2M,PT3M,PT4M,PT5M,PT6M";
  }
}
