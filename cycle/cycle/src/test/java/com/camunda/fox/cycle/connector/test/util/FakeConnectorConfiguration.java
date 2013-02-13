package com.camunda.fox.cycle.connector.test.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.camunda.fox.cycle.connector.vfs.VfsConnector;
import com.camunda.fox.cycle.entity.ConnectorConfiguration;

/**
 * Configuration for tests to not make autowiring of 
 * {@link ConnectorConfiguration} dependencies fail.
 * 
 * @author nico.rehwaldt
 */
@Configuration
public class FakeConnectorConfiguration {
  
  @Bean
  public ConnectorConfiguration vfsConnectorConfig () {
    ConnectorConfiguration config = new ConnectorConfiguration();
    config.getProperties().put(VfsConnector.BASE_PATH_KEY, VfsConnector.DEFAULT_BASE_PATH);
    return config;
  }
}
