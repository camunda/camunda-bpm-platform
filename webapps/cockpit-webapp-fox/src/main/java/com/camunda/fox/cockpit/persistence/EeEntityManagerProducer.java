package com.camunda.fox.cockpit.persistence;

import com.camunda.fox.cdi.transaction.impl.JtaTransactionEvent;
import com.camunda.fox.cdi.transaction.impl.JtaTransactionEvent.TransactionEventType;
import com.camunda.fox.cockpit.cdi.FoxEngineResource;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
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
  
  public void joinTransaction(@Observes JtaTransactionEvent transactionEvent) {
    if(TransactionEventType.AFTER_BEGIN == transactionEvent.getType()) {
      if(cockpitEntityManager != null) {
        cockpitEntityManager.joinTransaction();
      }
      if(foxEngineEntityManager != null) {
        foxEngineEntityManager.joinTransaction();
      }
    }
  }
  
  @PreDestroy
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
