package com.camunda.fox.cockpit.cdi.persistence;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
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
  
  @PersistenceUnit(unitName="fox-engine")
  private EntityManagerFactory foxEngineEntityManagerFactory;
  
  public EntityManager getFoxEngineEntityManager() {
    return foxEngineEntityManagerFactory.createEntityManager();
  }
  
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
