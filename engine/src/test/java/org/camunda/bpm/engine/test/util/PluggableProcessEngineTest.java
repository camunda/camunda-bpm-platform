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
package org.camunda.bpm.engine.test.util;

import java.io.FileNotFoundException;
import java.util.function.Function;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;


/**
 * Base class for the process engine JUnit 3 tests.
 */
public abstract class PluggableProcessEngineTest extends AbstractProcessEngineTestCase {

  protected static ProcessEngine cachedProcessEngine;
  protected String engineConfigurationResource;
  protected Function engineConfigurator;

  public PluggableProcessEngineTest() {
  }

  public PluggableProcessEngineTest(String engineConfigurationResource) {
    this(engineConfigurationResource, null);
  }

  public PluggableProcessEngineTest(String configurationResource, Function<ProcessEngineConfigurationImpl, Void> customizeConfiguration) {
    this.engineConfigurationResource = configurationResource;
    this.engineConfigurator = customizeConfiguration;
  }

  public static ProcessEngine getProcessEngine() {
    return getOrInitializeCachedProcessEngineWithTC();
  }

  @Override
  protected void initializeProcessEngine() {
    processEngine = getProcessEngine();
  }

  protected static ProcessEngine getOrInitializeCachedProcessEngineWithTC() {
    if (cachedProcessEngine == null) {
      ProcessEngineConfigurationImpl processEngineConfiguration;
      try {
        processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
            .createProcessEngineConfigurationFromResource("camunda.cfg.xml");
      } catch (RuntimeException ex) {
        if (ex.getCause() != null && ex.getCause() instanceof FileNotFoundException) {
          processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
              .createProcessEngineConfigurationFromResource("activiti.cfg.xml");
        } else {
          throw ex;
        }
      }

      cachedProcessEngine = processEngineConfiguration.buildProcessEngine();
    }
    return cachedProcessEngine;
  }

  @Override
  protected void closeDownProcessEngine() {
    if (engineConfigurationResource != null) {
      processEngine.close();
      processEngine = null;
    }
    super.closeDownProcessEngine();
  }
}