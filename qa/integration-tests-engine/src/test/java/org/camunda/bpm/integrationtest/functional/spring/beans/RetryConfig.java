package org.camunda.bpm.integrationtest.functional.spring.beans;

import org.springframework.stereotype.Component;

@Component
public class RetryConfig {
  public RetryConfig() {
  }

  public String defaultConfig() {
    return "PT1M,PT2M,PT3M,PT4M,PT5M,PT6M";
  }
}
