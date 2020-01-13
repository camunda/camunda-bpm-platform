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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Alexander Tyatenkov
 *
 */
public class CompositeDbHistoryEventHandlerTest extends AbstractCompositeHistoryEventHandlerTest {

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void shouldUseCompositeDbHistoryEventHandlerNonArgumentConstructor() {
    processEngineConfiguration.setHistoryEventHandler(new CompositeDbHistoryEventHandler());

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isZero();
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2L);
  }

  @Test
  public void shouldUseCompositeDbHistoryEventHandlerNonArgumentConstructorAddNullEvent() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler();
    try {
      compositeDbHistoryEventHandler.add(null);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertThat(e.getMessage()).containsIgnoringCase("History event handler is null");
    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void shouldUseCompositeDbHistoryEventHandlerNonArgumentConstructorAddNotNullEvent() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler();
    compositeDbHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(2);
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2L);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeDbHistoryEventHandlerNonArgumentConstructorAddTwoNotNullEvents() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler();
    compositeDbHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    compositeDbHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(4);
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2L);
  }

  @Test
  public void shouldUseCompositeDbHistoryEventHandlerArgumentConstructorWithNullVarargs() {
    HistoryEventHandler historyEventHandler = null;
    try {
      new CompositeDbHistoryEventHandler(historyEventHandler);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertThat(e.getMessage()).containsIgnoringCase("History event handler is null");
    }
  }

  @Test
  public void shouldUseCompositeDbHistoryEventHandlerArgumentConstructorWithNullTwoVarargs() {
    try {
      new CompositeDbHistoryEventHandler(null, null);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertThat(e.getMessage()).containsIgnoringCase("History event handler is null");
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeDbHistoryEventHandlerArgumentConstructorWithNotNullVarargsOneEvent() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(2);
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2L);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeDbHistoryEventHandlerArgumentConstructorWithNotNullVarargsTwoEvents() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler(new CustomDbHistoryEventHandler(), new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(4);
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2L);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void shouldUseCompositeDbHistoryEventHandlerArgumentConstructorWithEmptyList() {
    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler(new ArrayList<HistoryEventHandler>());
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isZero();
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2L);
  }

  @Test
  public void shouldUseCompositeDbHistoryEventHandlerArgumentConstructorWithNotEmptyListNullTwoEvents() {
    // prepare the list with two null events
    List<HistoryEventHandler> historyEventHandlers = new ArrayList<HistoryEventHandler>();
    historyEventHandlers.add(null);
    historyEventHandlers.add(null);

    try {
      new CompositeDbHistoryEventHandler(historyEventHandlers);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertThat(e.getMessage()).containsIgnoringCase("History event handler is null");
    }
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml"})
  public void shouldUseCompositeDbHistoryEventHandlerArgumentConstructorWithNotEmptyListNotNullTwoEvents() {
    // prepare the list with two events
    List<HistoryEventHandler> historyEventHandlers = new ArrayList<HistoryEventHandler>();
    historyEventHandlers.add(new CustomDbHistoryEventHandler());
    historyEventHandlers.add(new CustomDbHistoryEventHandler());

    CompositeDbHistoryEventHandler compositeDbHistoryEventHandler = new CompositeDbHistoryEventHandler(historyEventHandlers);
    processEngineConfiguration.setHistoryEventHandler(compositeDbHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(4);
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2L);
  }

}
