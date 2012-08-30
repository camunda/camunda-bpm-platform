package com.camunda.fox.cycle.repository;

import org.springframework.stereotype.Repository;

import com.camunda.fox.cycle.entity.ConnectorConfiguration;

@Repository
public class ConnectorConfigurationRepository extends AbstractRepository<ConnectorConfiguration>{
  
  public ConnectorConfiguration getConnectorConfiguration(String connectorId) {
    return em.createQuery("SELECT c FROM ConnectorConfiguration c WHERE connectorId = :connectorId", ConnectorConfiguration.class)
            .setParameter("connectorId", connectorId)
            .getSingleResult();
  }
}
