package com.camunda.fox.cycle.connector.test.util;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import com.camunda.fox.cycle.connector.Connector;
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
    for (ConnectorConfiguration configuration : defaultConfigurations) {
      repository.saveAndFlush(configuration);
    }
  }

  public void provideConnector(Class<? extends Connector> connectorCls, Connector connector) {
    List<ConnectorConfiguration> connectors = repository.findByConnectorClass(connectorCls.getName());
    
  }

  @PreDestroy
  public void remove() {
    System.out.println("Cleaning up connector configurations");
    repository.deleteAll();
  }
}
