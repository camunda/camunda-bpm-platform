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
import org.camunda.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

/**
 * @author Alexander Tyatenkov
 *
 */
public class CompositeHistoryEventHandlerTest extends AbstractCompositeHistoryEventHandlerTest {

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeHistoryEventHandlerNonArgumentConstructor() {
    processEngineConfiguration.setHistoryEventHandler(new CompositeHistoryEventHandler());

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isZero();
    assertThat(historyService.createHistoricDetailQuery().count()).isZero();
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseDefaultHistoryEventHandler() {
    // use default DbHistoryEventHandler
    processEngineConfiguration.setHistoryEventHandler(new DbHistoryEventHandler());

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isZero();
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2L);
  }

  @Test
  public void shouldUseCompositeHistoryEventHandlerNonArgumentConstructorAddNullEvent() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler();
    try {
      compositeHistoryEventHandler.add(null);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertThat(e.getMessage()).containsIgnoringCase("History event handler is null");
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeHistoryEventHandlerNonArgumentConstructorAddNotNullEvent() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler();
    compositeHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(2);
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(0);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeHistoryEventHandlerNonArgumentConstructorAddNotNullTwoEvents() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler();
    compositeHistoryEventHandler.add(new CustomDbHistoryEventHandler());
    compositeHistoryEventHandler.add(new DbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(2);
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2);
  }

  @Test
  public void shouldUseCompositeHistoryEventHandlerArgumentConstructorWithNullVarargs() {
    HistoryEventHandler historyEventHandler = null;
    try {
      new CompositeHistoryEventHandler(historyEventHandler);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertThat(e.getMessage()).containsIgnoringCase("History event handler is null");
    }
  }

  @Test
  public void shouldUseCompositeHistoryEventHandlerArgumentConstructorWithNullTwoVarargs() {
    try {
      new CompositeHistoryEventHandler(null, null);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertThat(e.getMessage()).containsIgnoringCase("History event handler is null");
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeHistoryEventHandlerArgumentConstructorWithNotNullVarargsOneEvent() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler(new CustomDbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(2);
    assertThat(historyService.createHistoricDetailQuery().count()).isZero();
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeHistoryEventHandlerArgumentConstructorWithNotNullVarargsTwoEvents() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler(new CustomDbHistoryEventHandler(), new DbHistoryEventHandler());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(2);
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2);
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeHistoryEventHandlerArgumentConstructorWithEmptyList() {
    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler(new ArrayList<HistoryEventHandler>());
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isZero();
    assertThat(historyService.createHistoricDetailQuery().count()).isZero();
  }

  @Test
  public void shouldUseCompositeHistoryEventHandlerArgumentConstructorWithNotEmptyListNullTwoEvents() {
    // prepare the list with two null events
    List<HistoryEventHandler> historyEventHandlers = new ArrayList<HistoryEventHandler>();
    historyEventHandlers.add(null);
    historyEventHandlers.add(null);

    try {
      new CompositeHistoryEventHandler(historyEventHandlers);
      fail("NullValueException expected");
    } catch (NullValueException e) {
      assertThat(e.getMessage()).containsIgnoringCase("History event handler is null");
    }
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/history/HistoryLevelTest.bpmn20.xml" })
  public void shouldUseCompositeHistoryEventHandlerArgumentConstructorWithNotEmptyListNotNullTwoEvents() {
    // prepare the list with two events
    List<HistoryEventHandler> historyEventHandlers = new ArrayList<HistoryEventHandler>();
    historyEventHandlers.add(new CustomDbHistoryEventHandler());
    historyEventHandlers.add(new DbHistoryEventHandler());

    CompositeHistoryEventHandler compositeHistoryEventHandler = new CompositeHistoryEventHandler(historyEventHandlers);
    processEngineConfiguration.setHistoryEventHandler(compositeHistoryEventHandler);

    startProcessAndCompleteUserTask();

    assertThat(countCustomHistoryEventHandler).isEqualTo(2);
    assertThat(historyService.createHistoricDetailQuery().count()).isEqualTo(2);
  }

}
