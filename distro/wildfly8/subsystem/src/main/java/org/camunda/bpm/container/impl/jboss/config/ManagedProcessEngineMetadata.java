/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.config;

import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.engine.ProcessEngineException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class ManagedProcessEngineMetadata {
  
  /** indicates whether the process engine should automatically create / 
   * update the database schema upon startup */ 
  public static String PROP_IS_AUTO_SCHEMA_UPDATE = "isAutoSchemaUpdate";

  /** indicates whether the identity module is used and if this tables are
   *  required */ 
  public static String PROP_IS_IDENTITY_USED = "isIdentityUsed";

  /** indicates whether the job executor should be automatically activated */
  public static String PROP_IS_ACTIVATE_JOB_EXECUTOR = "isActivateJobExecutor";
  
  /** the prefix to be used for all process engine database tables */
  public static String PROP_DB_TABLE_PREFIX = "dbTablePrefix";
  
  /** the name of the platform job executor acquisition to use */
  public static String PROP_JOB_EXECUTOR_ACQUISITION_NAME = "jobExecutorAcquisitionName";

  private boolean isDefault;
  private String engineName;
  private String datasourceJndiName;
  private String historyLevel;
  protected String configuration;
  private Map<String, String> configurationProperties;
  private Map<String, String> foxLegacyProperties;
  private List<ProcessEnginePluginXml> pluginConfigurations;

  /**
   * @param isDefault
   * @param engineName
   * @param datasourceJndiName
   * @param historyLevel
   * @param configuration
   * @param properties
   * @param pluginConfigurations
   */
  public ManagedProcessEngineMetadata(boolean isDefault, String engineName, String datasourceJndiName, String historyLevel, String configuration, Map<String, String> properties, List<ProcessEnginePluginXml> pluginConfigurations) {
    this.isDefault = isDefault;
    this.engineName = engineName;
    this.datasourceJndiName = datasourceJndiName;
    this.historyLevel = historyLevel;
    this.configuration = configuration;
    this.configurationProperties = selectProperties(properties, false);
    this.foxLegacyProperties = selectProperties(properties, true);
    this.pluginConfigurations = pluginConfigurations;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public String getEngineName() {
    return engineName;
  }

  public void setEngineName(String engineName) {
    this.engineName = engineName;
  }

  public String getDatasourceJndiName() {
    return datasourceJndiName;
  }

  public void setDatasourceJndiName(String datasourceJndiName) {
    this.datasourceJndiName = datasourceJndiName;
  }

  public String getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(String historyLevel) {
    this.historyLevel = historyLevel;
  }
  
  public String getConfiguration() {
    return configuration;
  }
  
  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public Map<String, String> getConfigurationProperties() {
    return configurationProperties;
  }

  public void setConfigurationProperties(Map<String, String> properties) {
    this.configurationProperties = properties;
  }
  
  public Map<String, String> getFoxLegacyProperties() {
    return foxLegacyProperties;
  }

  public void setFoxLegacyProperties(Map<String, String> foxLegacyProperties) {
    this.foxLegacyProperties = foxLegacyProperties;
  }

  public List<ProcessEnginePluginXml> getPluginConfigurations() {
    return pluginConfigurations;
  }

  public void setPluginConfigurations(List<ProcessEnginePluginXml> pluginConfigurations) {
    this.pluginConfigurations = pluginConfigurations;
  }

  public boolean isIdentityUsed() {
    Object object = getFoxLegacyProperties().get(PROP_IS_IDENTITY_USED);
    if(object == null) {
      return true;
    } else {
      return Boolean.parseBoolean((String) object);
    }
  }
  
  public boolean isAutoSchemaUpdate() {
    Object object = getFoxLegacyProperties().get(PROP_IS_AUTO_SCHEMA_UPDATE);
    if(object == null) {
      return true;
    } else {
      return Boolean.parseBoolean((String) object);
    }
  }
  
  public boolean isActivateJobExecutor() {
    Object object = getFoxLegacyProperties().get(PROP_IS_ACTIVATE_JOB_EXECUTOR);
    if(object == null) {
      return true;
    } else {
      return Boolean.parseBoolean((String) object);
    }
  }
  
  public String getDbTablePrefix() {
    Object object = getFoxLegacyProperties().get(PROP_DB_TABLE_PREFIX);
    if(object == null) {
      return null;
    } else {
      return (String) object;
    }
  }
  
  public String getJobExecutorAcquisitionName() {
    Object object = getFoxLegacyProperties().get(PROP_JOB_EXECUTOR_ACQUISITION_NAME);
    if(object == null) {
      return "default";
    } else {
      return (String) object;
    }
  }
  
  /**
   * validates the configuration and throws {@link ProcessEngineException} 
   * if the configuration is invalid.
   */
  public void validate() {
    StringBuilder validationErrorBuilder = new StringBuilder("Process engine configuration is invalid: \n");
    boolean isValid = true;    
    
    if(datasourceJndiName == null || datasourceJndiName.isEmpty()) {
      isValid = false;
      validationErrorBuilder.append(" property 'datasource' cannot be null \n");      
    }
    if(engineName == null || engineName.isEmpty()) {
      isValid = false;
      validationErrorBuilder.append(" property 'engineName' cannot be null \n");
    }

    for (int i = 0; i < pluginConfigurations.size(); i++) {
      ProcessEnginePluginXml pluginConfiguration = pluginConfigurations.get(i);
      if (pluginConfiguration.getPluginClass() == null || pluginConfiguration.getPluginClass().isEmpty()) {
        isValid = false;
        validationErrorBuilder.append(" property 'class' in plugin[" + i + "] cannot be null \n");
      }
    }
    
    if(!isValid) {
      throw new ProcessEngineException(validationErrorBuilder.toString());
    }
  }
  
  private Map<String, String> selectProperties(Map<String, String> allProperties, boolean selectFoxProperties) {
    Map<String, String> result = null;
    if (selectFoxProperties) {
      result = new HashMap<String, String>();
      String isAutoSchemaUpdate = allProperties.get(PROP_IS_AUTO_SCHEMA_UPDATE);
      String isActivateJobExecutor = allProperties.get(PROP_IS_ACTIVATE_JOB_EXECUTOR);
      String isIdentityUsed = allProperties.get(PROP_IS_IDENTITY_USED);
      String dbTablePrefix = allProperties.get(PROP_DB_TABLE_PREFIX);
      String jobExecutorAcquisitionName = allProperties.get(PROP_JOB_EXECUTOR_ACQUISITION_NAME);
      
      if (isAutoSchemaUpdate != null) {
        result.put(PROP_IS_AUTO_SCHEMA_UPDATE, isAutoSchemaUpdate);
      }
      if (isActivateJobExecutor != null) {
        result.put(PROP_IS_ACTIVATE_JOB_EXECUTOR, isActivateJobExecutor);
      }
      if (isIdentityUsed != null) {
        result.put(PROP_IS_IDENTITY_USED, isIdentityUsed);
      }
      if (dbTablePrefix != null) {
        result.put(PROP_DB_TABLE_PREFIX, dbTablePrefix);
      }
      if (jobExecutorAcquisitionName != null) {
        result.put(PROP_JOB_EXECUTOR_ACQUISITION_NAME, jobExecutorAcquisitionName);
      }
    } else {
      result = new HashMap<String, String>(allProperties);
      result.remove(PROP_IS_AUTO_SCHEMA_UPDATE);
      result.remove(PROP_IS_ACTIVATE_JOB_EXECUTOR);
      result.remove(PROP_IS_IDENTITY_USED);
      result.remove(PROP_DB_TABLE_PREFIX);
      result.remove(PROP_JOB_EXECUTOR_ACQUISITION_NAME);
    }
    return result;
  }

}
