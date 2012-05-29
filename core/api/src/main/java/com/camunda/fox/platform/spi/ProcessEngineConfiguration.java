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
package com.camunda.fox.platform.spi;

import java.util.Map;

/**
 * <p>The user-configuration of a process engine.</p>
 * 
 * @author Daniel Meyer
 */
public interface ProcessEngineConfiguration {

  /** indicates whether the process engine should automatically create / 
   * update the database schema upon startup */ 
  public static String PROP_IS_AUTO_SCHEMA_UPDATE = "isAutoSchemaUpdate";
  
  /** indicates whether the job executor should be automatically activated */
  public static String PROP_IS_ACTIVATE_JOB_EXECUTOR = "isActivateJobExecutor";
  
  /** the prefix to be used for all process engine database tables */
  public static String PROP_DB_TABLE_PREFIX = "dbTablePrefix";
  
  /** the name of the platform job executor acquisition to use */
  public static String PROP_JOB_EXECUTOR_ACQUISITION_NAME = "jobExecutorAcquisitionName";
  
  /**
   * @return true if this is the default process engine.
   * There can only be one default process engine per 
   * instance of the fox platform.
   */
  public boolean isDefault();
  
  /**
   * @return the name of the process engine
   */
  public String getProcessEngineName();
  
  /**
   * @return the jndi name of the datasource to use for the process engine
   */
  public String getDatasourceJndiName();
  
  /**
   * @return the history level to use
   */
  public String getHistoryLevel();
  
  /**
   * @return a {@link Map} of additional properties
   * 
   * @see #PROP_IS_AUTO_SCHEMA_UPDATE
   * @see #PROP_IS_ACTIVATE_JOB_EXECUTOR
   */
  public Map<String, Object> getProperties();
 

}
