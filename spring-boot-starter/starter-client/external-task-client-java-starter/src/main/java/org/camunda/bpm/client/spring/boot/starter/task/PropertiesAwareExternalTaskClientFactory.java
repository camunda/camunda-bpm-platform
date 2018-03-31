package org.camunda.bpm.client.spring.boot.starter.task;

import org.camunda.bpm.client.spring.ExternalTaskClientFactory;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Getter;
import lombok.Setter;

public class PropertiesAwareExternalTaskClientFactory extends ExternalTaskClientFactory {

  @Autowired
  private CamundaBpmClientProperties camundaBpmClientProperties;

  @Getter
  @Setter
  private String id;

  @Override
  public void afterPropertiesSet() throws Exception {
    camundaBpmClientProperties.getBaseUrl(getId()).ifPresent(this::setBaseUrl);
    super.afterPropertiesSet();
  }

}
