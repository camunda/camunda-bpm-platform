package com.camunda.fox.platform.impl.service.util;

import java.util.HashMap;
import java.util.Map;

import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * 
 * @author Daniel Meyer
 */
public class DummyProcessEngineConfiguration implements ProcessEngineConfiguration {
  
  private final String historyLevel;
  private final boolean isDefault;
  private final String processEngineName;
  private final String dataSourceJndiName;
  private final Map<String, Object> properties;

  public DummyProcessEngineConfiguration(boolean isDefault,
                                         String processEngineName,
                                         String dataSourceJndiName,
                                         String historyLevel,
                                         boolean isAutoSchemaUpdate,
                                         boolean isActivateJobExecutor) {
    this.isDefault = isDefault;
    this.processEngineName = processEngineName;
    this.dataSourceJndiName = dataSourceJndiName;
    this.historyLevel = historyLevel;
    
    properties = new HashMap<String, Object>();
    properties.put(PROP_IS_ACTIVATE_JOB_EXECUTOR, isActivateJobExecutor);
    properties.put(PROP_IS_AUTO_SCHEMA_UPDATE, isAutoSchemaUpdate);   
  }

  public boolean isDefault() {
    return isDefault;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public String getDatasourceJndiName() {
    return dataSourceJndiName;
  }

  public String getHistoryLevel() {
    return historyLevel;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

}
