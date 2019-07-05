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
package org.camunda.bpm.engine.test.jobexecutor;

import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineLoggingRule;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ReducedCmdExceptionLoggingTest {

  public ProcessEngineRule engineRule = new ProcessEngineRule();
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule().watch("org.camunda.bpm.engine.context", Level.DEBUG);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(loggingRule);

  private CommandExecutor commandExecutor;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    processEngineConfiguration = (ProcessEngineConfigurationImpl) engineRule.getProcessEngine().getProcessEngineConfiguration();
    commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setEnableReducedCmdExceptionLogging(false);
  }

  @Test
  public void shouldLogCatchedCmdException() {
    // given
    processEngineConfiguration.setEnableReducedCmdExceptionLogging(false);

    // when
    try {
      commandExecutor.execute(new FailingCmd());
    } catch (ProcessEngineException e) {
      // expected
    }

    List<ILoggingEvent> log = loggingRule.getFilteredLog("Expected");

    // then
    assertThat(log.size(), CoreMatchers.is(1));
  }

  @Test
  public void shouldNotLogCatchedCmdException() {
    // given
    processEngineConfiguration.setEnableReducedCmdExceptionLogging(true);

    // when
    try {
      commandExecutor.execute(new FailingCmd());
    } catch (ProcessEngineException e) {
      // expected
    }

    List<ILoggingEvent> log = loggingRule.getFilteredLog("Expected");
    
    // then
    assertThat(log.size(), CoreMatchers.is(0));
  }

  public class FailingCmd implements Command<Void> {
    @Override
    public Void execute(CommandContext commandContext) {
      throw new ProcessEngineException("Expected");
    }
  }
}
