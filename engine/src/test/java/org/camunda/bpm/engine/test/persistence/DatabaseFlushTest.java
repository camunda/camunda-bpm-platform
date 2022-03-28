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
package org.camunda.bpm.engine.test.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.concurrency.ConcurrencyTestCase;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class DatabaseFlushTest extends ConcurrencyTestCase {

  public static final BpmnModelInstance GW_PROCESS = Bpmn
      .createExecutableProcess("process")
      .startEvent()
      .parallelGateway()
      .userTask("task1-1")
      .userTask("task1-2")
      .endEvent()
      .moveToLastGateway()
      .userTask("task2-1")
      .userTask("task2-2")
      .endEvent()
      .done();

  /**
   * <p>This test reproduces a bug in which a batch of SQL operations
   * (here variable instance inserts) fail due to a constraint violation.
   * That constraint violation should be correctly treated as a case of
   * optimistic locking (variable name uniqueness in a scope).
   *
   * <p>In older versions this was not the case. Instead, the constraint
   * violation was incorrectly matched to a history
   * operation and therefore ignored, because we do not raise optimistic locking
   * exceptions for failed history operations. In consequence, we made an
   * incomplete runtime flush and the database got into an inconsistent
   * state.
   */
  @RequiredDatabase(excludes = DbSqlSessionFactory.DB2)
  @Test
  public void testNoIncompleteFlushOnConstraintViolation()
  {
    // given
   testRule.deploy(GW_PROCESS);

    runtimeService.startProcessInstanceByKey("process");

    List<Task> tasks = taskService.createTaskQuery().list();

    Task task1 = tasks.get(0);
    Task task2 = tasks.get(1);

    // three variables are required to reproduce the incorrect
    // matching of SQL failure to db operation
    VariableMap variables = Variables.createVariables()
        .putValue("key1", "val1")
        .putValue("key2", "val2")
        .putValue("key3", "val3");
    ThreadControl thread1 =
        executeControllableCommand(new CompleteTaskCommand(task1.getId(), variables));
    ThreadControl thread2 =
        executeControllableCommand(new CompleteTaskCommand(task2.getId(), variables));
    thread2.reportInterrupts();

    thread1.waitForSync();
    thread2.waitForSync();
    // both threads are now waiting before the flush

    // thread1 can successfully flush and inserts the variables
    thread1.waitUntilDone();

    // when
    // thread2 should encounter the constraint violation
    thread2.waitUntilDone();

    // then
    assertThat(thread2.getException()).isInstanceOf(OptimisticLockingException.class);

    // the task was not deleted, indicating that the database is still in a consistent state
    task2 = taskService.createTaskQuery().taskId(task2.getId()).singleResult();
    assertThat(task2).isNotNull();
  }

  private static class CompleteTaskCommand extends ControllableCommand<Void> {

    protected String taskId;
    protected VariableMap variables;

    public CompleteTaskCommand(String taskId, VariableMap variables)
    {
      this.taskId = taskId;
      this.variables = variables;
    }

    @Override
    public Void execute(CommandContext commandContext) {

      CompleteTaskCmd completeTaskCmd = new CompleteTaskCmd(taskId, variables);
      completeTaskCmd.execute(commandContext);

      monitor.sync();

      return null;
    }
  }

}

