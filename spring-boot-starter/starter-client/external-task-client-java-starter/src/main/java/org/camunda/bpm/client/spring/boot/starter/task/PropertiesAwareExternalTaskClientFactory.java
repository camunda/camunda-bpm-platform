package org.camunda.bpm.client.spring.boot.starter.task;

import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.camunda.bpm.client.spring.ExternalTaskClientFactory;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties.BasicAuthProperties;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties.Client;
import org.springframework.beans.factory.annotation.Autowired;

public class PropertiesAwareExternalTaskClientFactory extends ExternalTaskClientFactory {

  @Autowired
  private CamundaBpmClientProperties camundaBpmClientProperties;

  @Override
  public void afterPropertiesSet() throws Exception {
    camundaBpmClientProperties.getBaseUrl(getId()).ifPresent(this::setBaseUrl);
    addBasicAuthInterceptor();
    super.afterPropertiesSet();
  }

  protected void addBasicAuthInterceptor() {
    camundaBpmClientProperties.getClient(getId()).map(Client::getBasicAuth).filter(BasicAuthProperties::isEnabled)
        .ifPresent(basicAuth -> getClientRequestInterceptors().add(new BasicAuthProvider(basicAuth.getUsername(), basicAuth.getPassword())));
  }

}
