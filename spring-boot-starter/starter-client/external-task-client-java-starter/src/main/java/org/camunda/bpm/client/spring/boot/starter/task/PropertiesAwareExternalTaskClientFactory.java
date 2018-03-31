package org.camunda.bpm.client.spring.boot.starter.task;

import org.camunda.bpm.client.spring.ExternalTaskClientFactory;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties;
import org.springframework.beans.factory.annotation.Autowired;

public class PropertiesAwareExternalTaskClientFactory extends ExternalTaskClientFactory {

  @Autowired
  private CamundaBpmClientProperties camundaBpmClientProperties;

  @Override
  public void afterPropertiesSet() throws Exception {
    camundaBpmClientProperties.getBaseUrl(getId()).ifPresent(this::setBaseUrl);
    super.afterPropertiesSet();
  }

}
