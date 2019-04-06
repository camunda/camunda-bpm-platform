package org.camunda.bpm.client.spring;

import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.spring.extendedsubscription.ExtendedTestClassSubscription;
import org.camunda.bpm.client.spring.interceptor.ClientIdAcceptingClientRequestInterceptor;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan(basePackageClasses = { ExtendedTestClassSubscription.class })
public class ExtendedConfiguration {

  @Bean
  @Primary
  public ClientRequestInterceptor clientRequestInterceptor() {
    return new ClientRequestInterceptor() {

      @Override
      public void intercept(ClientRequestContext requestContext) {
        //
      }
    };
  }

  @Configuration
  @EnableTaskSubscription(baseUrl = "http://localhost:8080/rest", id = "First")
  static class FirstConfig {

    @TaskSubscription(topicName = "methodSubscription", externalTaskClientIds = "externalTaskClientFirst")
    @Bean
    public ExternalTaskHandler methodSubscription() {
      return (externalTask, externalTaskService) -> {

        // interact with the external task

      };
    }
  }

  @Configuration
  @EnableTaskSubscription(baseUrl = "http://localhost:8090/rest", id = SecondConfig.CLIENT_ID)
  static class SecondConfig {

    static final String CLIENT_ID = "Second";

    @Bean
    public ClientIdAcceptingClientRequestInterceptor clientIdAcceptingClientRequestInterceptor() {

      return new ClientIdAcceptingClientRequestInterceptor() {

        @Override
        public void intercept(ClientRequestContext requestContext) {
          //
        }

        @Override
        public boolean accepts(String id) {
          return CLIENT_ID.equals(id);
        }
      };
    }
  }

}
