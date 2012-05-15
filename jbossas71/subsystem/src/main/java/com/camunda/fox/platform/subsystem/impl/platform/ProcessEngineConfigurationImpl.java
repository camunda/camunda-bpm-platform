/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.subsystem.impl.platform;

import java.util.HashMap;
import java.util.Map;

import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * 
 * @author Daniel Meyer
 */
public class ProcessEngineConfigurationImpl implements ProcessEngineConfiguration {
  
  private final String historyLevel;
  private final boolean isDefault;
  private final String processEngineName;
  private final String dataSourceJndiName;
  private final Map<String, Object> properties;

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
