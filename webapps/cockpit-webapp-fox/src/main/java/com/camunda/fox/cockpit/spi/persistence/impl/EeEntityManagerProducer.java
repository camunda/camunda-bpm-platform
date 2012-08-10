package com.camunda.fox.cockpit.spi.persistence.impl;

import java.io.Serializable;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.camunda.fox.cdi.transaction.impl.JtaTransactionEvent;
import com.camunda.fox.cdi.transaction.impl.JtaTransactionEvent.TransactionEventType;

/**
 *
 * @author nico.rehwaldt
 * @author christian.lipphardt@camunda.com
 */
@ConversationScoped
public class EeEntityManagerProducer implements Serializable {
  
  private static final long serialVersionUID = 1L;

  @Inject
  private EeEntityManagerFactories entityManagerFactories;
  
  private EntityManager cockpitEntityManager;
  
  @Produces
  @RequestScoped
  public EntityManager getCockpitEntityManager() {
    if (cockpitEntityManager == null) {
      cockpitEntityManager = entityManagerFactories.getCockpitEntityManager();
    }
    return cockpitEntityManager;
  }
  
  @Produces
  @RequestScoped
  public EntityTransaction getTransaction() {
    return getCockpitEntityManager().getTransaction();
  }
  
  public void joinTransaction(@Observes JtaTransactionEvent transactionEvent) {
    if (TransactionEventType.AFTER_BEGIN == transactionEvent.getType()) {
      if(cockpitEntityManager != null) {
        cockpitEntityManager.joinTransaction();
      }
    }
  }
  
  @PreDestroy
  protected void preDestroy() {
    if (cockpitEntityManager != null && cockpitEntityManager.isOpen()) {
      cockpitEntityManager.close();
    }
  }
}
