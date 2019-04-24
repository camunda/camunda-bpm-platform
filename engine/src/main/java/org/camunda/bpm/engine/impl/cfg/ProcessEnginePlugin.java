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
package org.camunda.bpm.engine.impl.cfg;

import org.camunda.bpm.engine.ProcessEngine;

/**
 * <p>A process engine plugin allows customizing the process engine</p>
 * 
 * @author Daniel Meyer
 *
 */
public interface ProcessEnginePlugin {
  
  /**
   * <p>Invoked before the process engine configuration is initialized.</p>
   * 
   * @param processEngineConfiguration the process engine configuation
   * 
   */
  void preInit(ProcessEngineConfigurationImpl processEngineConfiguration);
  
  /**
   * <p>Invoked after the process engine configuration is initialized.
   * and before the process engine is built.</p>
   * 
   * @param processEngineConfiguration the process engine configuation
   * 
   */
  void postInit(ProcessEngineConfigurationImpl processEngineConfiguration);
  
  /**
   * <p>Invoked after the process engine has been built.</p>
   * 
   * @param processEngine
   */
  void postProcessEngineBuild(ProcessEngine processEngine);
  
}
