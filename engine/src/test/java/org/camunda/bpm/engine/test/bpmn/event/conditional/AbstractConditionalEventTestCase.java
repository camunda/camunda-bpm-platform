/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.bpmn.event.conditional;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class AbstractConditionalEventTestCase {

  protected static final String CONDITIONAL_EVENT_PROCESS_KEY = "conditionalEventProcess";
  protected static final String TASK_BEFORE_CONDITION = "Before Condition";
  protected static final String TASK_AFTER_CONDITION = "After Condition";
  protected static final String CONDITIONAL_EVENT = "conditionalEvent";
  protected static final String VARIABLE_NAME = "variable";

  @Rule
  public final ProcessEngineRule engine = new ProvidedProcessEngineRule();

  @Rule
  public ExpectedException expectException = ExpectedException.none();

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected EventSubscriptionQueryImpl conditionEventSubscriptionQuery;

  @Before
  public void init() {
    this.runtimeService = engine.getRuntimeService();
    this.taskService = engine.getTaskService();
    this.processEngineConfiguration = engine.getProcessEngineConfiguration();
    this.conditionEventSubscriptionQuery = new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutorTxRequired()).eventType(EventType.CONDITONAL.name());
  }

}
