package org.camunda.bpm.client.spring.boot.starter.task;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.camunda.bpm.client.spring.ExternalTaskClientFactory;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties.BasicAuthProperties;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientProperties.Client;
import org.camunda.bpm.client.spring.boot.starter.interceptor.IdAwareClientRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import lombok.Getter;
import lombok.Setter;

public class PropertiesAwareExternalTaskClientFactory extends ExternalTaskClientFactory {

  @Autowired
  private CamundaBpmClientProperties camundaBpmClientProperties;
  private List<IdAwareClientRequestInterceptor> idAwareClientRequestInterceptors = new ArrayList<>();

  @Getter
  @Setter
  private String id;

  @Override
  public void afterPropertiesSet() throws Exception {
    camundaBpmClientProperties.getBaseUrl(getId()).ifPresent(this::setBaseUrl);
    addBasicAuthInterceptor();
    super.afterPropertiesSet();
  }

  @Override
  protected void addClientRequestInterceptors(ExternalTaskClientBuilder taskClientBuilder) {
    super.addClientRequestInterceptors(taskClientBuilder);
    idAwareClientRequestInterceptors.forEach(taskClientBuilder::addInterceptor);
  }

  protected void addBasicAuthInterceptor() {
    camundaBpmClientProperties.getClient(getId()).map(Client::getBasicAuth).filter(BasicAuthProperties::isEnabled)
        .ifPresent(basicAuth -> getClientRequestInterceptors()
            .add(new BasicAuthProvider(basicAuth.getUsername(), basicAuth.getPassword())));
  }

  @Override
  public void setClientRequestInterceptors(List<ClientRequestInterceptor> clientRequestInterceptors) {
    if (CollectionUtils.isEmpty(clientRequestInterceptors)) {
      super.setClientRequestInterceptors(clientRequestInterceptors);
      return;
    }
    List<ClientRequestInterceptor> interceptors = new ArrayList<>();
    for (ClientRequestInterceptor clientRequestInterceptor : clientRequestInterceptors) {
      if (clientRequestInterceptor instanceof IdAwareClientRequestInterceptor) {
        IdAwareClientRequestInterceptor idAwareClientRequestInterceptor = (IdAwareClientRequestInterceptor) clientRequestInterceptor;
        if (idAwareClientRequestInterceptor.accepts(getId())) {
          idAwareClientRequestInterceptors.add(idAwareClientRequestInterceptor);
        }
      } else {
        interceptors.add(clientRequestInterceptor);
      }
    }
    super.setClientRequestInterceptors(interceptors);
  }
}
