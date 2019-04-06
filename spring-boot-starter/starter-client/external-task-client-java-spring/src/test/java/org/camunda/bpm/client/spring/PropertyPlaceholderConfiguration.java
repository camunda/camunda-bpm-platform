package org.camunda.bpm.client.spring;

import org.camunda.bpm.client.spring.subscription.TestClassSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
@ComponentScan(basePackageClasses = { TestClassSubscription.class })
@EnableTaskSubscription(baseUrl = "${client.baseUrl}")
public class PropertyPlaceholderConfiguration {

  @Bean
  static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    Resource location = new ClassPathResource("client.properties");
    configurer.setLocation(location);
    return configurer;
  }

  @TaskSubscription(topicName = "methodSubscription")
  @Bean
  public ExternalTaskHandler methodSubscription() {
    return (externalTask, externalTaskService) -> {

      // interact with the external task

    };
  }
}
