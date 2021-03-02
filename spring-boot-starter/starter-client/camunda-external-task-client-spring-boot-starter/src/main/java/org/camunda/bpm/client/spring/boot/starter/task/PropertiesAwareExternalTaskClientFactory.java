package org.camunda.bpm.client.spring.boot.starter.task;

import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.camunda.bpm.client.spring.ExternalTaskClientFactory;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties.BasicAuthProperties;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties.Client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PropertiesAwareExternalTaskClientFactory extends ExternalTaskClientFactory {

  @NonNull
  private final CamundaBpmClientProperties camundaBpmClientProperties;

  @Override
  public void afterPropertiesSet() throws Exception {
    camundaBpmClientProperties.getBaseUrl(getId()).ifPresent(this::setBaseUrl);
    camundaBpmClientProperties.getClient(getId()).ifPresent(this::applyClientProperties);
    addBasicAuthInterceptor();
    super.afterPropertiesSet();
  }

  protected void addBasicAuthInterceptor() {
    camundaBpmClientProperties.getClient(getId()).map(Client::getBasicAuth).filter(BasicAuthProperties::isEnabled)
        .ifPresent(basicAuth -> getClientRequestInterceptors().add(new BasicAuthProvider(basicAuth.getUsername(), basicAuth.getPassword())));
  }

  protected void applyClientProperties(Client client) {
    if (client != null) {
      if (client.getMaxTasks() != null) {
        setMaxTasks(client.getMaxTasks());
      }
      if (client.getWorkerId() != null) {
        setWorkerId(client.getWorkerId());
      }
      if (client.getAsyncResponseTimeout() != null) {
        setAsyncResponseTimeout(client.getAsyncResponseTimeout());
      }
      if (client.getAutoFetchingEnabled() != null) {
        setAutoFetchingEnabled(client.getAutoFetchingEnabled());
      }
      if (client.getLockDuration() != null) {
        setLockDuration(client.getLockDuration());
      }
      if (client.getDateFormat() != null) {
        setDateFormat(client.getDateFormat());
      }
      if (client.getDefaultSerializationFormat() != null) {
        setDefaultSerializationFormat(client.getDefaultSerializationFormat());
      }
    }
  }

}
