/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public abstract class AbstractCompositeHistoryEventHandlerTest extends PluggableProcessEngineTestCase {

  protected HistoryEventHandler originalHistoryEventHandler;

  /**
   * The counter used to check the amount of triggered events.
   */
  protected int countCustomHistoryEventHandler;

  /**
   * Perform common setup.
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // save current history event handler
    originalHistoryEventHandler = processEngineConfiguration.getHistoryEventHandler();
    // clear the event counter
    countCustomHistoryEventHandler = 0;
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
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
