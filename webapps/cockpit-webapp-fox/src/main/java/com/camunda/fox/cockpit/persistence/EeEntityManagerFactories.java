package com.camunda.fox.cockpit.persistence;

import javax.enterprise.inject.Specializes;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/**
 *
 * @author nico.rehwaldt
 */
@Specializes
public class EeEntityManagerFactories extends CockpitEntityManagerFactories {

  @PersistenceUnit(unitName="fox-cockpit")
  private EntityManagerFactory cockpitEntityManagerFactory;
  
  public EntityManager getCockpitEntityManager() {
    return cockpitEntityManagerFactory.createEntityManager();
  }

  @Override
  @Specializes
  public EntityManagerFactory getDefaultEntityManagerFactory() {
    return cockpitEntityManagerFactory;
  }

  @Override
  public synchronized void close() {
    ; // managed do not close
  }
}
