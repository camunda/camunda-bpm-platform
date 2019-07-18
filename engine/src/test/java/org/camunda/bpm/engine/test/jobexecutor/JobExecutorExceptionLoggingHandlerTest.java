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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobHelper;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class JobExecutorExceptionLoggingHandlerTest {
  
  public ProcessEngineRule engineRule = new ProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected ExecuteJobHelper.ExceptionLoggingHandler originalHandler;
  protected TweetNestedCommandExceptionHandler cmdExceptionHandler = new TweetNestedCommandExceptionHandler();
  
  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    originalHandler = ExecuteJobHelper.LOGGING_HANDLER;
    processEngineConfiguration.getJobHandlers().put(cmdExceptionHandler.getType(), cmdExceptionHandler);
  }

  @Test
  public void shouldBeAbleToReplaceLoggingHandler() {
    CollectingHandler collectingHandler = new CollectingHandler();
    ExecuteJobHelper.LOGGING_HANDLER = collectingHandler;
    
 // given
    final String jobId = processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<String>() {
      public String execute(CommandContext commandContext) {
        MessageEntity message = new MessageEntity();
        message.setJobHandlerType(TweetNestedCommandExceptionHandler.TYPE);
        commandContext.getJobManager().insertJob(message);
        return message.getId();
      }
    });

    // when
    processEngineConfiguration.getJobExecutor().start();
    testRule.waitForJobExecutorToProcessAllJobs();
    processEngineConfiguration.getJobExecutor().shutdown();

    Throwable collectedException = collectingHandler.collectedExceptions.get(jobId);

    assertTrue(collectedException instanceof RuntimeException);
    assertThat(collectedException.getMessage(), is("nested command exception"));

    // cleanup
    ExecuteJobHelper.LOGGING_HANDLER = originalHandler;
    engineRule.getManagementService().deleteJob(jobId);

    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogByJobId(jobId);
        return null;
      }
    });

    processEngineConfiguration.getJobHandlers().remove(cmdExceptionHandler.getType());
  }

  static class CollectingHandler implements ExecuteJobHelper.ExceptionLoggingHandler {

    Map<String, Throwable> collectedExceptions = new HashMap<String, Throwable>();

    @Override
    public void exceptionWhileExecutingJob(String jobId, Throwable exception) {
      collectedExceptions.put(jobId, exception);
    }

  }

}
