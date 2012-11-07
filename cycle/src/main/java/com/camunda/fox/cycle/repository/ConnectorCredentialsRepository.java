package com.camunda.fox.cycle.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.camunda.fox.cycle.entity.ConnectorCredentials;

/**
 * FIXME this class is untested! 
 *
 */
@Repository
public class ConnectorCredentialsRepository extends AbstractRepository<ConnectorCredentials> {
  
  public List<ConnectorCredentials> findFetchConfigurationByUserId(Long userId) {
    return em.createQuery("SELECT c FROM ConnectorCredentials c JOIN FETCH c.user JOIN FETCH c.connectorConfiguration WHERE c.user.id = :userId", ConnectorCredentials.class)
            .setParameter("userId", userId)
            .getResultList();
  }

  public ConnectorCredentials findFetchConfigurationById(Long id) {
    return em.createQuery("SELECT c FROM ConnectorCredentials c JOIN FETCH c.user JOIN FETCH c.connectorConfiguration WHERE c.id = :id", ConnectorCredentials.class)
            .setParameter("id", id)
            .getSingleResult();
  }

  public ConnectorCredentials findFetchAllByUserIdAndConnectorId(long userId, long connectorId) {
    return em.createQuery("SELECT c FROM ConnectorCredentials c JOIN FETCH c.user JOIN FETCH c.connectorConfiguration WHERE c.user.id = :userId AND c.connectorConfiguration.id = :connectorId", ConnectorCredentials.class)
            .setParameter("userId", userId)
            .setParameter("connectorId", connectorId)
            .getSingleResult();
  }
  
  public ConnectorCredentials findFetchAllByUsernameAndConnectorId(String username, long connectorId) {
    return em.createQuery("SELECT c FROM ConnectorCredentials c JOIN FETCH c.user JOIN FETCH c.connectorConfiguration WHERE c.user.name = :username AND c.connectorConfiguration.id = :connectorId", ConnectorCredentials.class)
            .setParameter("username", username)
            .setParameter("connectorId", connectorId)
            .getSingleResult();
  }
}
