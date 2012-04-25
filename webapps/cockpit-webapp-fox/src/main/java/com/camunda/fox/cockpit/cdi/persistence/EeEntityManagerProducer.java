package com.camunda.fox.cockpit.cdi.persistence;

import com.camunda.fox.cockpit.cdi.FoxEngineResource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
public class EeEntityManagerProducer extends CockpitEntityManagerProducer {
  
  @Inject
  private EeEntityManagerFactories entityManagerFactories;
  
  private EntityManager cockpitEntityManager;
  private EntityManager foxEngineEntityManager;
  
  @Specializes
  @Produces
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
  @Produces
  @RequestScoped
  @Override
  public EntityManager getCockpitEntityManager() {
    if (cockpitEntityManager == null) {
      cockpitEntityManager = entityManagerFactories.getCockpitEntityManager();
    }
    
    return cockpitEntityManager;
  }
    
  @Override
  @PreDestroy
  protected void preDestroy() {
    if (foxEngineEntityManager != null && foxEngineEntityManager.isOpen()) {
      foxEngineEntityManager.close();
    }
    
    if (cockpitEntityManager != null && cockpitEntityManager.isOpen()) {
      cockpitEntityManager.close();
    }
  }
  
  @Override
  public EntityTransaction getTransaction() {
    return super.getTransaction();
  }
}
