package com.camunda.fox.cycle.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.camunda.fox.cycle.entity.ConnectorCredentials;
import com.camunda.fox.cycle.entity.User;

@Repository
public class ConnectorCredentialsRepository extends AbstractRepository<ConnectorCredentials> {
  
  public List<ConnectorCredentials> fetchConnectorCredentialsByUser(Long userId) {
    User user = em.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.connectorCredentials WHERE u.id = :userId", User.class)
            .setParameter("userId", userId)
            .getSingleResult();
    return user.getConnectorCredentials();
  }
  

}
