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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DefaultHistoryEventHandlerTest {

  @Parameterized.Parameters
  public static Iterable<Object> parameters() {
    return Arrays.asList(new Object[]{
        true, false
    });
  }

  @Parameterized.Parameter
  public boolean isDefaultHandlerEnabled;

  @Rule
  public ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    // given
    configuration.setEnableDefaultDbHistoryEventHandler(isDefaultHandlerEnabled);
    configuration.setCustomHistoryEventHandlers(Collections.singletonList(new CustomHistoryEventHandler()));
  });

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  @Test
  public void shouldUseInstanceOfCompositeHistoryEventHandler() {
    // when
    boolean useDefaultDbHandler = engineRule.getProcessEngineConfiguration()
        .isEnableDefaultDbHistoryEventHandler();
    HistoryEventHandler defaultHandler = engineRule.getProcessEngineConfiguration()
        .getHistoryEventHandler();

    // then
    assertThat(useDefaultDbHandler).isNotNull();
    if (useDefaultDbHandler) {
      assertThat(defaultHandler).isInstanceOf(CompositeDbHistoryEventHandler.class);
    } else {
      assertThat(defaultHandler).isInstanceOf(CompositeHistoryEventHandler.class);
    }
  }

  @Test
  public void shouldProvideCustomHistoryEventHandlers() {
    // when
    List<HistoryEventHandler> eventHandlers = engineRule.getProcessEngineConfiguration().getCustomHistoryEventHandlers();

    // then
    assertThat(eventHandlers).hasSize(1);
    assertThat(eventHandlers.get(0)).isInstanceOf(CustomHistoryEventHandler.class);
  }

  public class CustomHistoryEventHandler implements HistoryEventHandler {

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
    }

    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
    }
  }
}