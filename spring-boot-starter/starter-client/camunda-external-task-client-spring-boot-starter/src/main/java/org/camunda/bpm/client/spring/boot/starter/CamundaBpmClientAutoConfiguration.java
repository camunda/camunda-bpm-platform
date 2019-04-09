package org.camunda.bpm.client.spring.boot.starter;

import org.camunda.bpm.client.spring.DisableDefaultExternalTaskRegistration;
import org.camunda.bpm.client.spring.EnableExternalTaskSubscriptions;
import org.camunda.bpm.client.spring.boot.starter.CamundaBpmClientAutoConfiguration.PropertiesAwareClientRegistrar;
import org.camunda.bpm.client.spring.boot.starter.task.PropertiesAwareExternalTaskClientFactory;
import org.camunda.bpm.client.spring.boot.starter.task.PropertiesAwareSubscribedExternalTaskBean;
import org.camunda.bpm.client.spring.context.ClientRegistrar;
import org.camunda.bpm.client.spring.context.ExternalTaskBeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;

@EnableConfigurationProperties({ CamundaBpmClientProperties.class })
@EnableExternalTaskSubscriptions
@DisableDefaultExternalTaskRegistration
@Import({ PropertiesAwareClientRegistrar.class })
@Configuration
public class CamundaBpmClientAutoConfiguration {

  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public static BeanDefinitionRegistryPostProcessor externalTaskBeanDefinitionRegistryPostProcessor() {
    return new ExternalTaskBeanDefinitionRegistryPostProcessor(PropertiesAwareSubscribedExternalTaskBean.class);
  }

  static class PropertiesAwareClientRegistrar extends ClientRegistrar {
    public PropertiesAwareClientRegistrar() {
      super(PropertiesAwareExternalTaskClientFactory.class);
    }

  }
}
