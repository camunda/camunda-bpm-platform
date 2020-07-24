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
package org.camunda.bpm.engine.impl.test;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class ResourceProcessEngineTestCase extends AbstractProcessEngineTestCase {
  /**
   * This class isn't used in the Process Engine test suite anymore.
   * However, some Test classes in the following modules still use it:
   *   * camunda-engine-plugin-spin
   *   * camunda-identity-ldap
   *
   * It should be removed once those Test classes are migrated to JUnit 4.
   */

  protected String engineConfigurationResource;

  public ResourceProcessEngineTestCase(String configurationResource) {
    this.engineConfigurationResource = configurationResource;
  }

  @Override
  protected void closeDownProcessEngine() {
    super.closeDownProcessEngine();
    processEngine.close();
    processEngine = null;
  }

  @Override
  protected void initializeProcessEngine() {
    ProcessEngineConfigurationImpl processEngineConfig = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResource(engineConfigurationResource);
    processEngine = processEngineConfig.buildProcessEngine();
  }

}
