package com.camunda.fox.platform.qa.deployer.war.impl;

import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;
import com.camunda.fox.platform.qa.deployer.war.ContextExecutionException;
import com.camunda.fox.platform.qa.deployer.war.ApplicationArchiveContext;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * <p>Singleton bean which allows to execute callbacks in the context the carrier process archive.</p>
 *
 * @author Daniel Meyer
 * @see ProcessArchive#execute(ProcessArchiveCallback)
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ApplicationArchiveContextImpl implements ApplicationArchiveContext {

  @Override
  public <T> T execute(ProcessArchiveCallback<T> callback) throws ContextExecutionException {
    try {
      return callback.execute();
    } catch (RuntimeException e) {
      throw new ContextExecutionException("Caught Exception", e);
    }
  }
  
  @Override
  public String getAppName() {
    try {
      return InitialContext.doLookup("java:app/AppName");
    } catch (NamingException e) {
      throw new RuntimeException("Failed to lookup application name", e);
    }
  }
  
  @Override
  public ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
}