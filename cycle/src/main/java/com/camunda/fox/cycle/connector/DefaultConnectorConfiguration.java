package com.camunda.fox.cycle.connector;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;

@Configuration
public class DefaultConnectorConfiguration {
  
  @Bean
  public ConnectorConfiguration vfsConnectorConfig () {
    ConnectorConfiguration config = new ConnectorConfiguration();
    config.getProperties().put(VfsConnector.BASE_PATH_KEY, VfsConnector.DEFAULT_BASE_PATH);
    config.setGlobalUser("user");
    config.setGlobalPassword("password");
    return config;
  }
  
}
