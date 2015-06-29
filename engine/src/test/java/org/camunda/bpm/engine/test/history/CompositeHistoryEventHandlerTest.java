/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.test.history;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Alexander Tyatenkov
 *
 */
public class CompositeHistoryEventHandlerTest extends PluggableProcessEngineTestCase {

  /**
   * The counter used to check the amount of triggered events.
   */
  private int countCustomHistoryEventHandler;

  /**
   * A {@link HistoryEventHandler} implementation to count the history events.
   */
  private class CustomDbHistoryEventHandler implements HistoryEventHandler {
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

  /**
   * Perform common setup.
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // clear the event counter
    countCustomHistoryEventHandler = 0;
  }

  /**
   * Tears down the fixture.
   */
  protected void tearDown() throws Exception {
    // Clean the cachedProcessEngine since test cases make changes on the
    // process engine configuration object. The engine unit tests are not
    // isolated with respect to the engine configuration, meaning that when you
    // change the configuration it may affect following test cases.
    cachedProcessEngine.close();
    cachedProcessEngine = null;
    super.tearDown();
  }

  /**
   * The helper method to execute the test task.
   */
  private void startProcessAndCompleteUserTask() {
    runtimeService.startProcessInstanceByKey("HistoryLevelTest");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testDefaultHistoryEventHandler() {
    // use default DbHistoryEventHandler
    processEngineConfiguration.setHistoryEventHandler(new DbHistoryEventHandler());

    startProcessAndCompleteUserTask();

    assertEquals(0, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerNonArgumentConstructor() {
    processEngineConfiguration.setHistoryEventHandler(new CompositeHistoryEventHandler());

    startProcessAndCompleteUserTask();

    assertEquals(0, countCustomHistoryEventHandler);
    assertEquals(0, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerNonArgumentConstructorAddNullEvent() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler();
    try {
      compositeHistoryEventHandler.add(null);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertTextPresent("History event handler is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerNonArgumentConstructorAddNotNullEvent() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler();
    compositeHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(2, countCustomHistoryEventHandler);
    assertEquals(0, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerNonArgumentConstructorAddNotNullTwoEvents() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler();
    compositeHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    compositeHistoryEventHandler.add(new DbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(2, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerArgumentConstructorWithNullVarargs() {
    HistoryEventHandler historyEventHandler = null;
    try {
      new CompositeHistoryEventHandler(historyEventHandler);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertTextPresent("History event handler is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerArgumentConstructorWithNullTwoVarargs() {
    try {
      new CompositeHistoryEventHandler(null, null);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertTextPresent("History event handler is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerArgumentConstructorWithNotNullVarargsOneEvent() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(2, countCustomHistoryEventHandler);
    assertEquals(0, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerArgumentConstructorWithNotNullVarargsTwoEvents() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler(new CustomDbHistoryEventHandler(), new DbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(2, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerArgumentConstructorWithEmptyList() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler(new ArrayList<HistoryEventHandler>());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(0, countCustomHistoryEventHandler);
    assertEquals(0, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerArgumentConstructorWithNotEmptyListNullTwoEvents() {
    // prepare the list with two null events
    List<HistoryEventHandler> historyEventHandlers = new ArrayList<HistoryEventHandler>();
    historyEventHandlers.add(null);
    historyEventHandlers.add(null);

    try {
      new CompositeHistoryEventHandler(historyEventHandlers);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertTextPresent("History event handler is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeHistoryEventHandlerArgumentConstructorWithNotEmptyListNotNullTwoEvents() {
    // prepare the list with two events
    List<HistoryEventHandler> historyEventHandlers = new ArrayList<HistoryEventHandler>();
    historyEventHandlers.add(new CustomDbHistoryEventHandler());
    historyEventHandlers.add(new DbHistoryEventHandler());

    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler(historyEventHandlers);
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(2, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

}
