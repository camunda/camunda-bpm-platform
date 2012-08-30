package com.camunda.fox.cycle.connector;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;

@Configuration
public class DefaultConnectorConfiguration {
  
  @Bean
  public ConnectorConfiguration vfsConnectorConfig () {
    ConnectorConfiguration config = new ConnectorConfiguration();
    config.getProperties().put("BASE_PATH", "file://" + System.getProperty("user.home") + File.separatorChar + "cycle" + File.separatorChar);
    config.setGlobalUser("user");
    config.setGlobalPassword("password");
    return config;
  }
  
}
