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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.RequiredDatabase;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.junit.Test;

/**
 * We only test Serializable Transaction Isolation on CockroachDB.
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
@RequiredDatabase(includes = DbSqlSessionFactory.CRDB)
public class TransactionIsolationSerializableTest extends ConcurrencyTestCase {

  protected static final String PROC_DEF_KEY = "oneTaskProcess";
  protected static final String VAR_NAME = "testVariableName";
  protected static final String VAR_INIT_VAL = "initialValue";
  protected static final String VAR_FIRST_VAL = "firstValue";
  protected static final String VAR_SECOND_VAL = "secondValue";

  /**
   * In this test, we run two transactions concurrently.
   * The transactions have the following behavior:
   *
   * (1) READ row from a table
   * (2) WRITE (update) the row from that table
   *
   * We execute it with two threads in the following interleaving:
   *
   *      Thread 1             Thread 2
   *      ========             ========
   * ------READ---------------------------
   * ---------------------------READ------
   * ------WRITE--------------------------
   * ---------------------------WRITE----- | FAILS due to concurrent update
   *
   * Data may become inconsistent if transactions are not ordered properly
   */
  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/oneTaskProcess.bpmn20.xml")
  public void shouldHandleConcurrentWriteConflictWithTX2Failure() {
    // given
    String processInstanceId = runtimeService
        .startProcessInstanceByKey(PROC_DEF_KEY, Collections.singletonMap(VAR_NAME, VAR_INIT_VAL))
        .getId();
    ThreadControl updateVarThread1 = executeControllableCommand(
        new ControllableVariableWriteCommand(processInstanceId, VAR_FIRST_VAL));
    updateVarThread1.waitForSync();
    updateVarThread1.reportInterrupts();
    ThreadControl updateVarThread2 = executeControllableCommand(
        new ControllableVariableWriteCommand(processInstanceId, VAR_SECOND_VAL));
    updateVarThread2.waitForSync();
    updateVarThread2.reportInterrupts();

    // the first thread updates the variable and flushes the changes
    updateVarThread1.makeContinue();
    updateVarThread1.waitUntilDone();

    // when
    // the second thread attempts to update the variable
    updateVarThread2.makeContinue();
    updateVarThread2.waitUntilDone();

    // then
    // a exception is expected with a `TransactionRetryWithProtoRefreshError` as the cause
    assertThat(updateVarThread2.getException().getCause().getMessage())
        .containsIgnoringCase("TransactionRetryWithProtoRefreshError");
    VariableInstance var = runtimeService.createVariableInstanceQuery().variableName(VAR_NAME).singleResult();
    assertThat(var.getValue()).isEqualTo(VAR_FIRST_VAL);
  }

  /**
   * In this test, we run two transactions concurrently.
   * The transactions have the following behavior:
   *
   * (1) READ row from a table
   * (2) WRITE (update) the row from that table
   *
   * We execute it with two threads in the following interleaving:
   *
   *      Thread 1             Thread 2
   *      ========             ========
   * ------READ---------------------------
   * ---------------------------READ------
   * ------WRITE-------------------------- | FAILS due to an unrelated, runtime error and is rolled back.
   * ---------------------------WRITE----- | PASSES since there are no concurrent changes.
   *
   * Data may become inconsistent if transactions are not ordered properly
   */
  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/oneTaskProcess.bpmn20.xml")
  public void shouldHandleConcurrentWriteConflictWithTX1Failure() {
    // given
    String processInstanceId = runtimeService
        .startProcessInstanceByKey(PROC_DEF_KEY, Collections.singletonMap(VAR_NAME, VAR_INIT_VAL))
        .getId();
    ThreadControl updateVarThread1 = executeControllableCommand(
        new ControllableVariableWriteCommand(processInstanceId, VAR_FIRST_VAL, true));
    updateVarThread1.waitForSync();
    updateVarThread1.reportInterrupts();
    ThreadControl updateVarThread2 = executeControllableCommand(
        new ControllableVariableWriteCommand(processInstanceId, VAR_SECOND_VAL));
    updateVarThread2.waitForSync();
    updateVarThread2.reportInterrupts();

    // when
    // the first thread updates the variable,
    // but fails with an unrelated exception and is rolled-back
    updateVarThread1.makeContinue();
    updateVarThread1.waitUntilDone();

    // the second thread attempts to update the variable
    updateVarThread2.makeContinue();
    updateVarThread2.waitUntilDone();

    // then
    // the second transaction is successful
    assertThat(updateVarThread1.getException()).isInstanceOf(RuntimeException.class);
    assertThat(updateVarThread2.getException()).isNull();

    VariableInstance var = runtimeService.createVariableInstanceQuery().variableName(VAR_NAME).singleResult();
    assertThat(var.getValue()).isEqualTo(VAR_SECOND_VAL);
  }

  /**
   * In this test, we run two transactions concurrently.
   * The transactions have the following behavior:
   *
   * (1) READ row from a table
   * (2) WRITE (update) the row from that table
   *
   * We execute it with two threads in the following interleaving:
   *
   *        TX1                  TX2
   *        TS1        <         TS2 = TS1+N     | TX1 has a lower timestamp
   *      ========             ========
   * ---------------------------READ------ | performs a read with a higher timestamp
   * ------WRITE-------------------------- | TS1 is pushed to TS2 + 1
   * ---------------------------WRITE----- | will fail since TX1 made changes (out-of-scope for this case)
   *
   * Data may become inconsistent if transactions are not ordered properly
   */
  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/concurrency/oneTaskProcess.bpmn20.xml")
  public void shouldHandleConcurrentWriteAfterReadConflict() throws InterruptedException {
    // given
    String processInstanceId = runtimeService
        .startProcessInstanceByKey(PROC_DEF_KEY, Collections.singletonMap(VAR_NAME, VAR_INIT_VAL))
        .getId();

    // TX1 with a lower TX timestamp
    ThreadControl updateVarThread1 = executeControllableCommand(
        new ControllableVariableWriteCommand(processInstanceId, VAR_FIRST_VAL, false, false));
    updateVarThread1.waitForSync();
    updateVarThread1.reportInterrupts();

    // Introduce a larger difference between TX timestamps
    Thread.sleep(1000L);

    // TX2 with a higher TX timestamp that first performs a READ
    ThreadControl updateVarThread2 = executeControllableCommand(
        new ControllableVariableWriteCommand(processInstanceId, VAR_SECOND_VAL, false, true));
    updateVarThread2.waitForSync();
    updateVarThread2.reportInterrupts();

    // when
    // TX1 attempts to write a new variable value
    updateVarThread1.makeContinue();
    updateVarThread1.waitUntilDone();

    // then
    // the TX1 timestamp is set to TX2_timestamp + 1, and TX1 is successful
    // NOTE: it's not possible to verify this since the timestamp is not available until after the TX is committed
    assertThat(updateVarThread1.getException()).isNull();

    VariableInstance var = runtimeService.createVariableInstanceQuery().variableName(VAR_NAME).singleResult();
    assertThat(var.getValue()).isEqualTo(VAR_FIRST_VAL);

    // cleanup
    updateVarThread2.waitUntilDone(true);
  }

  public class ControllableVariableWriteCommand extends ControllableCommand<Void> {

    protected String processInstanceId;
    protected String newVariableValue;
    protected boolean rollback;
    protected boolean readVariable;

    ControllableVariableWriteCommand(String processInstanceId, String newVariableValue) {
      this(processInstanceId, newVariableValue, false, true);
    }

    ControllableVariableWriteCommand(String processInstanceId, String newVariableValue, boolean rollback) {
      this(processInstanceId, newVariableValue, rollback, true);
    }

    ControllableVariableWriteCommand(String processInstanceId, String newVariableValue, boolean rollback, boolean readVariable) {
      this.processInstanceId = processInstanceId;
      this.newVariableValue = newVariableValue;
      this.rollback = rollback;
      this.readVariable = readVariable;
    }

    public Void execute(CommandContext commandContext) {

      // add a read entry for the TX in the CRDB timestamp log
      if (readVariable) {
        commandContext.getProcessEngineConfiguration()
            .getRuntimeService()
            .createVariableInstanceQuery()
            .processInstanceIdIn(processInstanceId)
            .variableName(VAR_NAME)
            .list();
      }

      monitor.sync();

      commandContext.getProcessEngineConfiguration()
          .getRuntimeService()
          .setVariable(processInstanceId, VAR_NAME, newVariableValue);

      if (rollback) {
        throw new RuntimeException();
      }

      return null;
    }

  }

}
