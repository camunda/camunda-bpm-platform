package com.camunda.fox.cockpit.spi.persistence.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/**
 *
 * @author nico.rehwaldt
 */
@ApplicationScoped
public class EeEntityManagerFactories {

  @PersistenceUnit(unitName="fox-cockpit")
  private EntityManagerFactory cockpitEntityManagerFactory;
  
  public EntityManager getCockpitEntityManager() {
    return cockpitEntityManagerFactory.createEntityManager();
  }

  @Produces
  @ApplicationScoped
  public EntityManagerFactory getDefaultEntityManagerFactory() {
    return cockpitEntityManagerFactory;
  }
}
