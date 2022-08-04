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
package org.camunda.bpm.engine.test.concurrency;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class BuiltinExceptionCodeForeignKeyConstraintViolationTest extends ConcurrencyTestCase {

  protected static class ControllableDeleteProcessDefinitionCommand extends ControllableCommand<Void> {

    protected String processDefinitionId;

    protected Exception exception;

    public ControllableDeleteProcessDefinitionCommand(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }

    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called from main thread

      commandContext.getProcessEngineConfiguration()
          .getRepositoryService()
          .deleteProcessDefinition(processDefinitionId);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

  public static class ControllableStartProcessInstanceCommand extends ControllableCommand<Void> {

    protected String processDefinitionKey;

    public ControllableStartProcessInstanceCommand(String processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
    }

    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called from main thread

      commandContext.getProcessEngineConfiguration()
          .getRuntimeService()
          .startProcessInstanceByKey(processDefinitionKey);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }
  }

  /**
   * This test case doesn't lead to a foreign key constraint violation on CRDB.
   * Instead, a deadlock error is detected.
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @RequiredDatabase(excludes = {DbSqlSessionFactory.CRDB})
  @Test
  public void shouldReturnForeignKeyConstraintErrorCode() {
    // given
    ThreadControl thread1 = executeControllableCommand(new ControllableStartProcessInstanceCommand("oneTaskProcess"));
    thread1.reportInterrupts();
    thread1.waitForSync();

    String processDefinitionKey = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("oneTaskProcess")
        .singleResult()
        .getId();

    ThreadControl thread2 = executeControllableCommand(new ControllableDeleteProcessDefinitionCommand(processDefinitionKey));
    thread2.reportInterrupts();
    thread2.waitForSync();

    //start process instance, but not commit transaction
    thread1.makeContinue();
    thread1.waitForSync();

    //delete process definition, but not commit transaction
    thread2.makeContinue();
    thread2.waitForSync();

    //commit transaction that starts a process instance
    thread1.makeContinue();
    thread1.waitUntilDone();

    // when: try to commit the transaction that deletes a process definition
    thread2.makeContinue();
    thread2.waitUntilDone();

    // then
    assertThat(thread2.exception)
        .isInstanceOf(ProcessEngineException.class)
        .extracting("code")
        .contains(BuiltinExceptionCode.FOREIGN_KEY_CONSTRAINT_VIOLATION.getCode());
  }

}
