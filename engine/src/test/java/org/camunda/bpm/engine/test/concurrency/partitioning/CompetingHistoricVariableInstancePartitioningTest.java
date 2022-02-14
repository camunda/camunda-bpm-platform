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
package org.camunda.bpm.engine.test.concurrency.partitioning;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Tassilo Weidner
 */

public class CompetingHistoricVariableInstancePartitioningTest extends AbstractPartitioningTest {

  protected final String VARIABLE_NAME = "aVariableName";
  final protected String VARIABLE_VALUE = "aVariableValue";
  final protected String ANOTHER_VARIABLE_VALUE = "anotherVariableValue";

  @Test
  public void shouldSuppressOleOnConcurrentFetchAndDelete() {
    // given
    String processInstanceId = deployAndStartProcess(PROCESS_WITH_USERTASK,
      Variables.createVariables().putValue(VARIABLE_NAME, VARIABLE_VALUE)).getId();

    ThreadControl asyncThread = executeControllableCommand(new AsyncThread(processInstanceId));
    asyncThread.reportInterrupts();
    asyncThread.waitForSync();

    commandExecutor.execute((Command<Void>) commandContext -> {
      HistoricVariableInstanceEntity historicVariableInstanceEntity =
        (HistoricVariableInstanceEntity) historyService.createHistoricVariableInstanceQuery().singleResult();

      commandContext.getDbEntityManager().delete(historicVariableInstanceEntity);

      return null;
    });

    // assume
    assertThat(historyService.createHistoricVariableInstanceQuery().singleResult()).isNull();

    // when
    asyncThread.makeContinue();
    asyncThread.waitUntilDone();

    // then
    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertThat(runtimeService.createVariableInstanceQuery().singleResult().getName()).isEqualTo(VARIABLE_NAME);
      assertThat(runtimeService.createVariableInstanceQuery().singleResult().getValue()).isEqualTo(ANOTHER_VARIABLE_VALUE);
    } else {
      // with CockroachDB, the OLE can't be ignored, the TX will fail and be rolled-back
      assertThat(asyncThread.getException()).isInstanceOf(CrdbTransactionRetryException.class);
    }
  }

  public class AsyncThread extends ControllableCommand<Void> {

    String processInstanceId;

    AsyncThread(String processInstanceId) {
      this.processInstanceId = processInstanceId;
    }

    public Void execute(CommandContext commandContext) {
     historyService.createHistoricVariableInstanceQuery()
        .singleResult()
        .getId(); // cache

      monitor.sync();

      commandContext.getProcessEngineConfiguration()
        .getRuntimeService()
        .setVariable(processInstanceId, VARIABLE_NAME, ANOTHER_VARIABLE_VALUE);

      return null;
    }

  }

}
