package org.camunda.bpm.integrationtest.functional.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.junit.Assert;

@Named
@ApplicationScoped
public class PersistenceDelegateBean implements JavaDelegate {

  @PersistenceContext
  private EntityManager em;
  
  private SomeEntity entity;

  public void execute(DelegateExecution execution) throws Exception {

    // we assert that the entity manager contains the entity
    // this means that we obtain the same entity manager we used to
    // persist the entity before starting the process

    Assert.assertTrue(em.contains(entity));

  }

  public void setEntity(SomeEntity entity) {
    this.entity = entity;
  }

}
