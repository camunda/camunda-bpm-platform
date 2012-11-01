package com.camunda.fox.cycle.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.camunda.fox.cycle.entity.ConnectorCredentials;

@Repository
public class ConnectorCredentialsRepository extends AbstractRepository<ConnectorCredentials> {
  
  public List<ConnectorCredentials> fetchConnectorCredentialsByUserId(Long userId) {
    return em.createQuery("SELECT c FROM ConnectorCredentials c JOIN FETCH c.user JOIN FETCH c.connectorConfiguration WHERE c.user.id = :userId", ConnectorCredentials.class)
            .setParameter("userId", userId)
            .getResultList();
  }

  public ConnectorCredentials fetchConnectorCredentialsById(Long id) {
    return em.createQuery("SELECT c FROM ConnectorCredentials c JOIN FETCH c.user JOIN FETCH c.connectorConfiguration WHERE c.id = :id", ConnectorCredentials.class)
            .setParameter("id", id)
            .getSingleResult();
  }

  public ConnectorCredentials fetchConnectorCredentialsByUserIdAndConnectorId(Long userId, Long connectorId) {
    return em.createQuery("SELECT c FROM ConnectorCredentials c JOIN FETCH c.user JOIN FETCH c.connectorConfiguration WHERE c.user.id = :userId AND c.connectorConfiguration.id = :connectorId", ConnectorCredentials.class)
            .setParameter("userId", userId)
            .setParameter("connectorId", connectorId)
            .getSingleResult();
  }

}
