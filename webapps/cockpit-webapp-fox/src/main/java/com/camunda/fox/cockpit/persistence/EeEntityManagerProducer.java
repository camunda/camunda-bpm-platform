package com.camunda.fox.cockpit.persistence;

import com.camunda.fox.cockpit.cdi.FoxEngineResource;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 *
 * @author nico.rehwaldt
 */
@Specializes
@ConversationScoped
public class EeEntityManagerProducer extends CockpitEntityManagerProducer {
  
  @Inject
  private EeEntityManagerFactories entityManagerFactories;
  
  private EntityManager cockpitEntityManager;
  private EntityManager foxEngineEntityManager;
  
  @Specializes
  @FoxEngineResource
  @RequestScoped
  @Override
  public EntityManager getFoxEngineEntityManager() {
    if (foxEngineEntityManager == null) {
      foxEngineEntityManager = entityManagerFactories.getFoxEngineEntityManager();
    }
    return foxEngineEntityManager;
  }
  
  @Specializes
  @RequestScoped
  @Override
  public EntityManager getCockpitEntityManager() {
    if (cockpitEntityManager == null) {
      cockpitEntityManager = entityManagerFactories.getCockpitEntityManager();
    }
    return cockpitEntityManager;
  }

  @Override
  @Specializes
  public EntityTransaction getTransaction() {
    return getCockpitEntityManager().getTransaction();
  }
  
  @Override
  protected void preDestroy() {
    if (cockpitEntityManager != null && cockpitEntityManager.isOpen()) {
      cockpitEntityManager.close();
    }
    
    if (foxEngineEntityManager != null && foxEngineEntityManager.isOpen()) {
      foxEngineEntityManager.close();
    }
  }
}
