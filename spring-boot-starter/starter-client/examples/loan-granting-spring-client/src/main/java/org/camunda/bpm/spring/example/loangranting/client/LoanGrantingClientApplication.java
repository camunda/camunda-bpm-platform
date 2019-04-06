package org.camunda.bpm.spring.example.loangranting.client;

import org.camunda.bpm.client.spring.EnableTaskSubscription;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
@ComponentScan
@EnableTaskSubscription(baseUrl = "${client.baseUrl}")
public class LoanGrantingClientApplication {

  public static void main(String[] args) {
    new AnnotationConfigApplicationContext(LoanGrantingClientApplication.class);
  }

  @Bean
  static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    Resource location = new ClassPathResource("client.properties");
    configurer.setLocation(location);
    return configurer;
  }

}
