package com.camunda.fox.cycle.connector.test.util;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;
import com.camunda.fox.cycle.repository.ConnectorConfigurationRepository;


/**
 *
 * @author nico.rehwaldt
 */
public class ConnectorConfigurationProvider {

  @Inject
  private ConnectorConfigurationRepository repository;
  
  @Inject
  private List<ConnectorConfiguration> defaultConfigurations;
  
  @PostConstruct
  public void persist() {
    System.out.println("Persisting connector configurations: " + defaultConfigurations);
    
    for (ConnectorConfiguration configuration : defaultConfigurations) {
      repository.saveAndFlush(configuration);
    }
  }
  
  @PreDestroy
  public void remove() {
    System.out.println("Cleaning up connector configurations");
    
    repository.deleteAll();
  }
}
