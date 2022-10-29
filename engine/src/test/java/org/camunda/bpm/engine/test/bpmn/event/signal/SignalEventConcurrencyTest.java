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
package org.camunda.bpm.engine.test.bpmn.event.signal;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.event.EventHandler;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestHelper;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class SignalEventConcurrencyTest extends ConcurrencyTestHelper {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule();
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected EventHandler signalEventHandler;
  protected EventHandler evSpy;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    super.init();

    // create a spy from the default SignalEventHandler & set it in the config
    signalEventHandler = processEngineConfiguration.getEventHandler("signal");
    evSpy = Mockito.spy(signalEventHandler);
    processEngineConfiguration.getEventHandlers().put("signal", evSpy);
  }

  @After
  public void tearDown() {
    processEngineConfiguration.getEventHandlers().put("signal", signalEventHandler);
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventConcurrencyTest.testSignalWithCompletedExecution.bpmn20.xml" })
  @RequiredDatabase(excludes = DbSqlSessionFactory.CRDB)
  public void shouldThrowExceptionWhenSignallingWithCompletedExecution() {

    runtimeService.startProcessInstanceByKey("mainProcess");

    final ControllableCommand<Object> sendSignalCommand = new ControllableCommand<Object>() {
      @Override
      public Object execute(final CommandContext commandContext) {
        // send signal to execution
        final Execution execution = runtimeService.createExecutionQuery().activityId("IdActAlex").singleResult();
        runtimeService.createSignalEvent("effectCreatedSignal")
            .executionId(execution.getId())
            .send();

        return null;
      }
    };

    // stub the handleEvent method of the SignalEventHandler & block it until we complete the task in the main thread
    Mockito.doAnswer(invocation -> {
      // thread will block here until makeContinue() is called form main thread
      sendSignalCommand.getMonitor().sync();

      return invocation.callRealMethod();
    }).when(evSpy).handleEvent(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    // send the signal in a separate thread & wait until it reaches our breakpoint (sync()) in the SignalEventHandler
    ThreadControl signalThread = executeControllableCommand(sendSignalCommand);
    signalThread.reportInterrupts();
    signalThread.waitForSync();

    // complete the only task in the process, i.e. complete the execution
    final Task mainTask = taskService.createTaskQuery().active().singleResult();
    taskService.complete(mainTask.getId());

    // unblock the second thread in the handleEvent
    signalThread.makeContinue();
    signalThread.waitUntilDone(true);

    // sending the signal will fail because it cannot find the execution anymore
    final Throwable exception = signalThread.getException();
    assertThat(exception).isInstanceOf(NullValueException.class);
    assertThat(exception).hasMessage(
        String.format("Cannot restore state of process instance %s: list of executions is empty",
            mainTask.getProcessInstanceId()));
  }

}
