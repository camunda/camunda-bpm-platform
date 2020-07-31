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
package org.camunda.bpm.engine.test.standalone.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

/**
 * @author Tom Baeyens
 */
public class CommandContextInterceptorTest extends PluggableProcessEngineTest {

  @Test
  public void testCommandContextGetCurrentAfterException() {
    try {
      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          throw new IllegalStateException("here i come!");
        }
      });

      fail("expected exception");
    } catch (IllegalStateException e) {
      // OK
    }

    assertNull(Context.getCommandContext());
  }

  @Test
  public void testCommandContextNestedFailingCommands() {
    final ExceptionThrowingCmd innerCommand1 = new ExceptionThrowingCmd(new IdentifiableRuntimeException(1));
    final ExceptionThrowingCmd innerCommand2 = new ExceptionThrowingCmd(new IdentifiableRuntimeException(2));

    try {
      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          CommandExecutor commandExecutor = Context.getProcessEngineConfiguration().getCommandExecutorTxRequired();

          commandExecutor.execute(innerCommand1);
          commandExecutor.execute(innerCommand2);

          return null;
        }
      });

      fail("Exception expected");
    } catch (IdentifiableRuntimeException e) {
      assertEquals(1, e.id);
    }

    assertTrue(innerCommand1.executed);
    assertFalse(innerCommand2.executed);
  }

  @Test
  public void testCommandContextNestedTryCatch() {
    final ExceptionThrowingCmd innerCommand = new ExceptionThrowingCmd(new IdentifiableRuntimeException(1));

    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        CommandExecutor commandExecutor = Context.getProcessEngineConfiguration().getCommandExecutorTxRequired();

        try {
          commandExecutor.execute(innerCommand);
          fail("exception expected to pop up during execution of inner command");
        } catch (IdentifiableRuntimeException e) {
          // happy path
          assertNull("the exception should not have been propagated to this command's context",
              Context.getCommandInvocationContext().getThrowable());
        }

        return null;
      }
    });
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  @Test
  public void testCommandContextNestedFailingCommandsNotExceptions() {
    final BpmnModelInstance modelInstance =
      Bpmn.createExecutableProcess("processThrowingThrowable")
        .startEvent()
          .serviceTask()
          .camundaClass(ThrowErrorJavaDelegate.class)
        .endEvent().done();

   testRule.deploy(modelInstance);

    boolean errorThrown = false;
    try {
      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {

          runtimeService.startProcessInstanceByKey("processThrowingThrowable");
          return null;
        }
      });
      fail("Exception expected");
    } catch (StackOverflowError t) {
      //OK
      errorThrown = true;
    }

    assertTrue(ThrowErrorJavaDelegate.executed);
    assertTrue(errorThrown);

    // Check data base consistency
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().count());
  }

  protected class ExceptionThrowingCmd implements Command<Void> {

    protected boolean executed;

    protected RuntimeException exceptionToThrow;

    public ExceptionThrowingCmd(RuntimeException e) {
      executed = false;
      exceptionToThrow = e;
    }

    public Void execute(CommandContext commandContext) {
      executed = true;
      throw exceptionToThrow;
    }

  }

  protected class IdentifiableRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    protected int id;
    public IdentifiableRuntimeException(int id) {
      this.id = id;
    }
  }

}
