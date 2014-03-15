package org.camunda.bpm.integrationtest.functional.jpa;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.junit.Assert;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Named
@ApplicationScoped
public class AsyncPersistenceDelegateBean implements JavaDelegate {

  @PersistenceContext
  private EntityManager em;
  
  private SomeEntity entity;

  public void execute(DelegateExecution execution) throws Exception {

    // we assert that the entity manager does not contain the entity
    // this means that we obtain a seperate entity manager since
    // we are invoked in a new transaction

    Assert.assertFalse(em.contains(entity));

  }

  public void setEntity(SomeEntity entity) {
    this.entity = entity;
  }

}
