package com.camunda.fox.platform.qa.deployer.war;

import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 *
 * @author nico.rehwaldt
 */
public interface ApplicationArchiveContext {

  /**
   * Execute the given statement within the context of the application archive
   * 
   * @param <T>
   * @param callback
   * @return
   * @throws ContextExecutionException 
   */
  public <T> T execute(ProcessArchiveCallback<T> callback) throws ContextExecutionException;
  
  /**
   * Return the class loader of the application archive
   * @return 
   */
  public ClassLoader getClassLoader();
  
  /**
   * Returns the application name
   * @return 
   */
  public String getAppName();
}
