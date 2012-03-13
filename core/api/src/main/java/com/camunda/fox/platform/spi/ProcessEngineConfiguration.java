package com.camunda.fox.platform.spi;

/**
 * <p>The user-configuration of a process engine.</p>
 * 
 * @author Daniel Meyer
 */
public interface ProcessEngineConfiguration {
  
  public boolean isDefault();
  
  public String getProcessEngineName();
  
  public String getDatasourceJndiName();
  
  public String getHistoryLevel(); 
  
  // TODO: add more properties here.

}
