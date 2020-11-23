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
package org.camunda.bpm.engine.test.api.history.removaltime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_NONE;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.mockito.Mockito.mock;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryRemovalTimeProvider;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Tassilo Weidner
 */
public class RemovalTimeStrategyConfigurationTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected static ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    processEngineConfiguration
      .setHistoryRemovalTimeStrategy(null)
      .setHistoryRemovalTimeProvider(null)
      .initHistoryRemovalTime();
  }

  @AfterClass
  public static void tearDown() {
    processEngineConfiguration
      .setHistoryRemovalTimeStrategy(null)
      .setHistoryRemovalTimeProvider(null)
      .initHistoryRemovalTime();
  }

  @Test
  public void shouldAutomaticallyConfigure() {
    // given

    processEngineConfiguration
      .setHistoryRemovalTimeProvider(null)
      .setHistoryRemovalTimeStrategy(null);

    // when
    processEngineConfiguration.initHistoryRemovalTime();

    // then
    assertThat(processEngineConfiguration.getHistoryRemovalTimeStrategy(), is(HISTORY_REMOVAL_TIME_STRATEGY_END));
    assertThat(processEngineConfiguration.getHistoryRemovalTimeProvider(), isA(HistoryRemovalTimeProvider.class));
  }

  @Test
  public void shouldConfigureToStart() {
    // given

    processEngineConfiguration
      .setHistoryRemovalTimeProvider(mock(HistoryRemovalTimeProvider.class))
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START);

    // when
    processEngineConfiguration.initHistoryRemovalTime();

    // then
    assertThat(processEngineConfiguration.getHistoryRemovalTimeStrategy(), is(HISTORY_REMOVAL_TIME_STRATEGY_START));
    assertThat(processEngineConfiguration.getHistoryRemovalTimeProvider(), isA(HistoryRemovalTimeProvider.class));
  }

  @Test
  public void shouldConfigureToEnd() {
    // given

    processEngineConfiguration
      .setHistoryRemovalTimeProvider(mock(HistoryRemovalTimeProvider.class))
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END);

    // when
    processEngineConfiguration.initHistoryRemovalTime();

    // then
    assertThat(processEngineConfiguration.getHistoryRemovalTimeStrategy(), is(HISTORY_REMOVAL_TIME_STRATEGY_END));
    assertThat(processEngineConfiguration.getHistoryRemovalTimeProvider(), isA(HistoryRemovalTimeProvider.class));
  }

  @Test
  public void shouldConfigureToNone() {
    // given

    processEngineConfiguration
      .setHistoryRemovalTimeProvider(mock(HistoryRemovalTimeProvider.class))
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE);

    // when
    processEngineConfiguration.initHistoryRemovalTime();

    // then
    assertThat(processEngineConfiguration.getHistoryRemovalTimeStrategy(), is(HISTORY_REMOVAL_TIME_STRATEGY_NONE));
    assertThat(processEngineConfiguration.getHistoryRemovalTimeProvider(), isA(HistoryRemovalTimeProvider.class));
  }

  @Test
  public void shouldConfigureWithoutProvider() {
    // given

    processEngineConfiguration
      .setHistoryRemovalTimeProvider(null)
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END);

    // when
    processEngineConfiguration.initHistoryRemovalTime();

    // then
    assertThat(processEngineConfiguration.getHistoryRemovalTimeStrategy(), is(HISTORY_REMOVAL_TIME_STRATEGY_END));
    assertThat(processEngineConfiguration.getHistoryRemovalTimeProvider(), isA(HistoryRemovalTimeProvider.class));
  }

  @Test
  public void shouldConfigureWithNotExistentStrategy() {
    // given
    processEngineConfiguration.setHistoryRemovalTimeStrategy("notExistentStrategy");

    // when/then
    assertThatThrownBy(() -> processEngineConfiguration.initHistoryRemovalTime())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("history removal time strategy must be set to 'start', 'end' or 'none'");

    // assume
    assertThat(processEngineConfiguration.getHistoryRemovalTimeProvider(), isA(HistoryRemovalTimeProvider.class));
  }

}
