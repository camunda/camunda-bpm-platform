package com.camunda.fox.cycle.connector.test.util;

import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import com.camunda.fox.cycle.connector.crypt.EncryptionService;
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
  
  @Inject
  private EncryptionService encryptionService;

  @PostConstruct
  public void persist() {
    for (ConnectorConfiguration configuration : defaultConfigurations) {
      repository.saveAndFlush(copy(configuration));
    }
  }

  public void ensurePersisted() {
    if (repository.countAll() == 0) {
      persist();
    }
  }
  
  @PreDestroy
  public void remove() {
    System.out.println("Cleaning up connector configurations");
    repository.deleteAll();
  }
  
  protected ConnectorConfiguration copy(ConnectorConfiguration config) {
    ConnectorConfiguration copy = new ConnectorConfiguration();
    copy.setConnectorClass(config.getConnectorClass());
    copy.setConnectorName(config.getConnectorName());
    copy.setGlobalPassword(encryptionService.encryptConnectorPassword(config.getGlobalPassword()));
    copy.setGlobalUser(config.getGlobalUser());
    copy.setLoginMode(config.getLoginMode());
    copy.setName(config.getName());
    copy.setProperties(new HashMap<String, String>(config.getProperties()));
    
    return copy;
  }
}
