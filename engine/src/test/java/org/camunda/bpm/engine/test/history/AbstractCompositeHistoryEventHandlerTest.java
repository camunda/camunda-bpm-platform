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
package org.camunda.bpm.engine.test.history;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public abstract class AbstractCompositeHistoryEventHandlerTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;

  protected HistoryEventHandler originalHistoryEventHandler;

  /**
   * The counter used to check the amount of triggered events.
   */
  protected int countCustomHistoryEventHandler;

  /**
   * Perform common setup.
   */
  @Before
  public void setUp() throws Exception {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();

    // save current history event handler
    originalHistoryEventHandler = processEngineConfiguration.getHistoryEventHandler();
    // clear the event counter
    countCustomHistoryEventHandler = 0;
  }

  @After
  public void tearDown() throws Exception {
    // reset original history event handler
    processEngineConfiguration.setHistoryEventHandler(originalHistoryEventHandler);
  }

  /**
   * The helper method to execute the test task.
   */
  protected void startProcessAndCompleteUserTask() {
    runtimeService.startProcessInstanceByKey("HistoryLevelTest");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
  }

  /**
   * A {@link HistoryEventHandler} implementation to count the history events.
   */
  protected class CustomDbHistoryEventHandler implements HistoryEventHandler {

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
      // take into account only variable related events
      if (historyEvent instanceof HistoricVariableUpdateEventEntity) {
        // emulate the history event processing and persisting
        countCustomHistoryEventHandler++;
      }
    }

    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
      for (HistoryEvent historyEvent : historyEvents) {
        handleEvent(historyEvent);
      }
    }

  }

}
