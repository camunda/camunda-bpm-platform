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
package org.camunda.bpm.engine.test.api.history.removaltime.cleanup;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_END_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_END;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_NONE;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
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
public class HistoryCleanupStrategyConfigurationTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected static ProcessEngineConfigurationImpl engineConfiguration;

  @Before
  public void init() {
    engineConfiguration = engineRule.getProcessEngineConfiguration();

    engineConfiguration
      .setHistoryCleanupStrategy(null)
      .setHistoryRemovalTimeStrategy(null)
      .initHistoryCleanup();
  }

  @AfterClass
  public static void tearDown() {
    engineConfiguration
      .setHistoryRemovalTimeStrategy(null)
      .initHistoryRemovalTime();
    engineConfiguration
      .setHistoryCleanupStrategy(null)
      .initHistoryCleanup();
  }

  @Test
  public void shouldAutomaticallyConfigure() {
    // given

    engineConfiguration
      .setHistoryCleanupStrategy(null);

    // when
    engineConfiguration.initHistoryCleanup();

    // then
    assertThat(engineConfiguration.getHistoryCleanupStrategy(), is(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED));
  }

  @Test
  public void shouldConfigureToRemovalTimeBased() {
    // given

    engineConfiguration
      .setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED);

    // when
    engineConfiguration.initHistoryCleanup();

    // then
    assertThat(engineConfiguration.getHistoryCleanupStrategy(), is(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED));
  }

  @Test
  public void shouldConfigureToRemovalTimeBasedWithRemovalTimeStrategyToEnd() {
    // given

    engineConfiguration
      .setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED)
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_END);

    // when
    engineConfiguration.initHistoryCleanup();

    // then
    assertThat(engineConfiguration.getHistoryCleanupStrategy(), is(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED));
    assertThat(engineConfiguration.getHistoryRemovalTimeStrategy(), is(HISTORY_REMOVAL_TIME_STRATEGY_END));
  }

  @Test
  public void shouldConfigureToRemovalTimeBasedWithRemovalTimeStrategyToStart() {
    // given

    engineConfiguration
      .setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED)
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_START);

    // when
    engineConfiguration.initHistoryCleanup();

    // then
    assertThat(engineConfiguration.getHistoryCleanupStrategy(), is(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED));
    assertThat(engineConfiguration.getHistoryRemovalTimeStrategy(), is(HISTORY_REMOVAL_TIME_STRATEGY_START));
  }


  @Test
  public void shouldConfigureToEndTimeBased() {
    // given

    engineConfiguration
      .setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_END_TIME_BASED);

    // when
    engineConfiguration.initHistoryCleanup();

    // then
    assertThat(engineConfiguration.getHistoryCleanupStrategy(), is(HISTORY_CLEANUP_STRATEGY_END_TIME_BASED));
  }

  @Test
  public void shouldConfigureWithNotExistentStrategy() {
    // given

    engineConfiguration
      .setHistoryCleanupStrategy("nonExistentStrategy");

    // when/then
    assertThatThrownBy(() -> engineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("history cleanup strategy must be either set to 'removalTimeBased' or 'endTimeBased'.");
  }

  @Test
  public void shouldConfigureToRemovalTimeBasedWithRemovalTimeStrategyToNone() {
    // given

    engineConfiguration
      .setHistoryCleanupStrategy(HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED)
      .setHistoryRemovalTimeStrategy(HISTORY_REMOVAL_TIME_STRATEGY_NONE);

    // when/then
    assertThatThrownBy(() -> engineConfiguration.initHistoryCleanup())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("history removal time strategy cannot be set to 'none' in conjunction with 'removalTimeBased' history cleanup strategy.");
  }

}
