package org.camunda.bpm.client.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.spring.interceptor.ClientIdAwareClientRequestInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import lombok.Getter;
import lombok.Setter;

public class ExternalTaskClientFactory implements FactoryBean<ExternalTaskClient>, InitializingBean {

  @Getter
  @Setter
  private String baseUrl;
  @Getter
  @Setter
  private String id;
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
    clientRequestInterceptors.stream().filter(filterClientRequestInterceptors()).forEach(taskClientBuilder::addInterceptor);
  }

  protected Predicate<ClientRequestInterceptor> filterClientRequestInterceptors() {
    Predicate<ClientRequestInterceptor> isAcceptingIdAware = clientRequestInterceptor -> (clientRequestInterceptor instanceof ClientIdAwareClientRequestInterceptor)
        && ((ClientIdAwareClientRequestInterceptor) clientRequestInterceptor).accepts(getId());

    Predicate<ClientRequestInterceptor> isNotIdAware = clientRequestInterceptor -> !(clientRequestInterceptor instanceof ClientRequestInterceptor);
    return isNotIdAware.or(isAcceptingIdAware);
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
    this.clientRequestInterceptors = CollectionUtils.isEmpty(clientRequestInterceptors) ? new ArrayList<>() : clientRequestInterceptors;
  }

}
