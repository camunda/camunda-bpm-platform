package com.camunda.fox.cycle.repository;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;

@Repository
public class ConnectorConfigurationRepository extends AbstractRepository<ConnectorConfiguration>{
  
  public List<ConnectorConfiguration> findByConnectorClass(String cls) {
    return em.createQuery("SELECT c FROM ConnectorConfiguration c WHERE c.connectorClass = :cls", ConnectorConfiguration.class)
            .setParameter("cls", cls)
            .getResultList();
  }
  
  /**
   * Removes all connector configurations
   * @return 
   */
  @Transactional
  @Override
  public int deleteAll() {
    List<ConnectorConfiguration> configurations = findAll();
    for (ConnectorConfiguration config : configurations) {
      delete(config);
    }
    
    return configurations.size();
  }
}
