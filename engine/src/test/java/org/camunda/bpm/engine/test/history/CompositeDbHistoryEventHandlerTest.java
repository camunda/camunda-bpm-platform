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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Alexander Tyatenkov
 *
 */
public class CompositeDbHistoryEventHandlerTest extends AbstractCompositeHistoryEventHandlerTest {

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void testCompositeDbHistoryEventHandlerNonArgumentConstructor() {
    processEngineConfiguration.setHistoryEventHandler(new CompositeDbHistoryEventHandler());

    startProcessAndCompleteUserTask();

    assertEquals(0, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

  public void testCompositeDbHistoryEventHandlerNonArgumentConstructorAddNullEvent() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler();
    try {
      compositeDbHistoryEventHandler.add(null);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertTextPresent("History event handler is null", e.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void testCompositeDbHistoryEventHandlerNonArgumentConstructorAddNotNullEvent() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler();
    compositeDbHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(2, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeDbHistoryEventHandlerNonArgumentConstructorAddTwoNotNullEvents() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler();
    compositeDbHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    compositeDbHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(4, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

  public void testCompositeDbHistoryEventHandlerArgumentConstructorWithNullVarargs() {
    HistoryEventHandler historyEventHandler = null;
    try {
      new CompositeDbHistoryEventHandler(historyEventHandler);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertTextPresent("History event handler is null", e.getMessage());
    }
  }

  public void testCompositeDbHistoryEventHandlerArgumentConstructorWithNullTwoVarargs() {
    try {
      new CompositeDbHistoryEventHandler(null, null);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertTextPresent("History event handler is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeDbHistoryEventHandlerArgumentConstructorWithNotNullVarargsOneEvent() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(2, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void testCompositeDbHistoryEventHandlerArgumentConstructorWithNotNullVarargsTwoEvents() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler(new CustomDbHistoryEventHandler(), new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(4, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void testCompositeDbHistoryEventHandlerArgumentConstructorWithEmptyList() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler(new ArrayList<HistoryEventHandler>());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(0, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

  public void testCompositeDbHistoryEventHandlerArgumentConstructorWithNotEmptyListNullTwoEvents() {
    // prepare the list with two null events
    List<HistoryEventHandler> historyEventHandlers = new ArrayList<HistoryEventHandler>();
    historyEventHandlers.add(null);
    historyEventHandlers.add(null);

    try {
      new CompositeDbHistoryEventHandler(historyEventHandlers);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertTextPresent("History event handler is null", e.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void testCompositeDbHistoryEventHandlerArgumentConstructorWithNotEmptyListNotNullTwoEvents() {
    // prepare the list with two events
    List<HistoryEventHandler> historyEventHandlers = new ArrayList<HistoryEventHandler>();
    historyEventHandlers.add(new CustomDbHistoryEventHandler());
    historyEventHandlers.add(new CustomDbHistoryEventHandler());

    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler(historyEventHandlers);
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertEquals(4, countCustomHistoryEventHandler);
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }

}
