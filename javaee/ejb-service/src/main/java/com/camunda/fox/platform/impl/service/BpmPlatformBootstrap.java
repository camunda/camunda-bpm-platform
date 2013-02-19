package com.camunda.fox.platform.impl.service;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * <p>Bootstrap for the camunda BPM platform as a Singleton EJB</p>
 *   
 * @author Daniel Meyer
 */
@Startup
@Singleton(name="BpmPlatformBootstrap")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class BpmPlatformBootstrap {

  final private static Logger log = Logger.getLogger(BpmPlatformBootstrap.class.getName());

  @PostConstruct
  public void start() {
  }
  
  @PreDestroy
  public void stop() {
  }

}
