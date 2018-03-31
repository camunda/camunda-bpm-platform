package org.camunda.bpm.client.spring;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.Setter;

public class ExternalTaskClientFactory implements FactoryBean<ExternalTaskClient>, InitializingBean {

  @Getter
  @Setter
  private String baseUrl;
  @Getter
  private List<ClientRequestInterceptor> clientRequestInterceptors = new ArrayList<>();
  private ExternalTaskClient externalTaskClient;

  @Override
  public ExternalTaskClient getObject() throws Exception {
    if (externalTaskClient == null) {
      ExternalTaskClientBuilder taskClientBuilder = ExternalTaskClient.create().baseUrl(baseUrl);
      addClientRequestInterceptors(taskClientBuilder);
      externalTaskClient = taskClientBuilder.build();
    }
    return externalTaskClient;
  }

  protected void addClientRequestInterceptors(ExternalTaskClientBuilder taskClientBuilder) {
    clientRequestInterceptors.forEach(taskClientBuilder::addInterceptor);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(baseUrl, "baseUrl must not be 'null'");
  }

  @Override
  public Class<ExternalTaskClient> getObjectType() {
    return ExternalTaskClient.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Autowired(required = false)
  public void setClientRequestInterceptors(List<ClientRequestInterceptor> clientRequestInterceptors) {
    this.clientRequestInterceptors = clientRequestInterceptors == null ? new ArrayList<>() : clientRequestInterceptors;
  }

}
