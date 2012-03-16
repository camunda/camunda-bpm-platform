package com.camunda.fox.platform.test.services.util;

import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * 
 * @author Daniel Meyer
 */
public class ProcessEngineConfigurationImpl implements ProcessEngineConfiguration {
  
  private final boolean isActivateJobExecutor;
  private final boolean isAutoSchemaUpdate;
  private final String historyLevel;
  private final boolean isDefault;
  private final String processEngineName;
  private final String dataSourceJndiName;

  public ProcessEngineConfigurationImpl(boolean isDefault,
                                         String processEngineName,
                                         String dataSourceJndiName,
                                         String historyLevel,
                                         boolean isAutoSchemaUpdate,
                                         boolean isActivateJobExecutor) {
    this.isDefault = isDefault;
    this.processEngineName = processEngineName;
    this.dataSourceJndiName = dataSourceJndiName;
    this.historyLevel = historyLevel;
    this.isAutoSchemaUpdate = isAutoSchemaUpdate;
    this.isActivateJobExecutor = isActivateJobExecutor;
   
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

  public boolean isAutoSchemaUpdate() {
    return isAutoSchemaUpdate;
  }

  public boolean isActivateJobExcutor() {
    return isActivateJobExecutor;
  }

}
