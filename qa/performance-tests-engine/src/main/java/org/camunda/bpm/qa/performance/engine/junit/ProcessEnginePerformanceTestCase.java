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
package org.camunda.bpm.qa.performance.engine.junit;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestBuilder;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestConfiguration;
import org.junit.Before;
import org.junit.Rule;

/**
 * <p>Base class for implementing a process engine performance test</p>
 *
 * @author Daniel Meyer, Ingo Richtsmeier
 *
 */
public abstract class ProcessEnginePerformanceTestCase {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule(PerfTestProcessEngine.getInstance());

  @Rule
  public PerfTestConfigurationRule testConfigurationRule = new PerfTestConfigurationRule();

  @Rule
  public PerfTestResultRecorderRule resultRecorderRule = new PerfTestResultRecorderRule();

  protected ProcessEngine engine;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;

  @Before
  public void setup() {
    engine = processEngineRule.getProcessEngine();
    taskService = engine.getTaskService();
    historyService = engine.getHistoryService();
    runtimeService = engine.getRuntimeService();
    repositoryService = engine.getRepositoryService();
  }

  protected PerfTestBuilder performanceTest() {
    PerfTestConfiguration configuration = testConfigurationRule.getPerformanceTestConfiguration();
    configuration.setPlatform("camunda BPM");
    return new PerfTestBuilder(configuration, resultRecorderRule);
  }

}
