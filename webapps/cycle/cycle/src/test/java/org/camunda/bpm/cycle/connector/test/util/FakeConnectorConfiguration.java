package org.camunda.bpm.cycle.connector.test.util;

import org.camunda.bpm.cycle.connector.vfs.VfsConnector;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
