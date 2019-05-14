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
package org.camunda.bpm.engine.test.standalone.db;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.management.PurgeReport;

/**
 * Plugin that ensures that the new database table (ACT_GE_SCHEMA_LOG)
 * introduced with 7.11 is ignored when running tests with the 7.10 engine and
 * the 7.11 schema.
 * TODO: Remove this after the 7.11 release.
 * 
 * @author Miklas Boskamp
 */
public class TestIgnoreSchemaLogTableOnPurgePlugin implements ProcessEnginePlugin {

  private class IgnoreSchemaLogTableOnPurgeManagementService extends ManagementServiceImpl {
    @Override
    public PurgeReport purge() {
      return commandExecutor.execute(new TestIgnoreSchemaLogTablePurgeDatabaseAndCacheCmd());
    }
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.setManagementService(new IgnoreSchemaLogTableOnPurgeManagementService());
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
  }
}