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

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.ProcessEngineRule;

public class ProvidedProcessEngineRule extends ProcessEngineRule {

  protected Callable<ProcessEngine> processEngineProvider;

  public ProvidedProcessEngineRule() {
    super(PluggableProcessEngineTest.getProcessEngine(), true);
  }

  public ProvidedProcessEngineRule(final ProcessEngineBootstrapRule bootstrapRule) {
    this(() -> bootstrapRule.getProcessEngine());
  }

  public ProvidedProcessEngineRule(Callable<ProcessEngine> processEngineProvider) {
    super(true);
    this.processEngineProvider = processEngineProvider;
  }

  @Override
  protected void initializeProcessEngine() {

    if (processEngineProvider != null) {
      try {
        this.processEngine = processEngineProvider.call();
      } catch (Exception e) {
        throw new RuntimeException("Could not get process engine", e);
      }
    }
    else {
      super.initializeProcessEngine();
    }
  }

}
