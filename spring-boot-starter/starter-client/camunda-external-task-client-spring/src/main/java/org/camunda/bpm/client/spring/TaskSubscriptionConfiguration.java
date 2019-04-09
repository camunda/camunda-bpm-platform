package org.camunda.bpm.client.spring;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.client.spring.context.ClientRegistrar;
import org.camunda.bpm.client.spring.context.ExternalTaskBeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class TaskSubscriptionConfiguration implements ImportSelector {

  @Override
  public String[] selectImports(AnnotationMetadata importingClassMetadata) {
    AnnotationAttributes enableTaskSubscription = ClientRegistrar.getEnableTaskSubscription(importingClassMetadata);
    List<String> imports = new ArrayList<>();

    if (!ClientRegistrar.getDisableDefaultExternalTaskRegistration(importingClassMetadata).isPresent()) {
      imports.add(PostProcessorConfig.class.getName());
    }

    if (!StringUtils.isEmpty(ClientRegistrar.getBaseUrl(enableTaskSubscription))) {
      imports.add(ClientRegistrar.class.getName());
    }

    return imports.toArray(new String[0]);
  }

  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  static class PostProcessorConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static BeanDefinitionRegistryPostProcessor externalTaskBeanDefinitionRegistryPostProcessor() {
      return new ExternalTaskBeanDefinitionRegistryPostProcessor();
    }

  }

}
