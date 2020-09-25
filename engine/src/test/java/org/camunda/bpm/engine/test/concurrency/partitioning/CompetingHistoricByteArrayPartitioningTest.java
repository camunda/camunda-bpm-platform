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
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Tassilo Weidner
 */

public class CompetingHistoricByteArrayPartitioningTest extends AbstractPartitioningTest {

  final protected String VARIABLE_NAME = "aVariableName";
  final protected String VARIABLE_VALUE = "aVariableValue";
  final protected String ANOTHER_VARIABLE_VALUE = "anotherVariableValue";

  @Test
  public void shouldSuppressOleOnConcurrentFetchAndDelete() {
    // given
    final String processInstanceId = deployAndStartProcess(PROCESS_WITH_USERTASK,
      Variables.createVariables().putValue(VARIABLE_NAME,
        Variables.byteArrayValue(VARIABLE_VALUE.getBytes())))
      .getId();

    final String[] historicByteArrayId = new String[1];
    commandExecutor.execute((Command<Void>) commandContext -> {

      ExecutionEntity execution = commandContext.getExecutionManager().findExecutionById(processInstanceId);

      VariableInstanceEntity varInstance = (VariableInstanceEntity) execution.getVariableInstance(VARIABLE_NAME);
      HistoricVariableInstanceEntity historicVariableInstance = commandContext.getHistoricVariableInstanceManager()
        .findHistoricVariableInstanceByVariableInstanceId(varInstance.getId());

      historicByteArrayId[0] = historicVariableInstance.getByteArrayValueId();

      return null;
    });

    ThreadControl asyncThread = executeControllableCommand(new AsyncThread(processInstanceId, historicByteArrayId[0]));
    asyncThread.reportInterrupts();
    asyncThread.waitForSync();

    commandExecutor.execute((Command<Void>) commandContext -> {

      commandContext.getByteArrayManager()
        .deleteByteArrayById(historicByteArrayId[0]);

      return null;
    });

    commandExecutor.execute((Command<Void>) commandContext -> {

      // assume
      assertThat(commandContext.getDbEntityManager().selectById(ByteArrayEntity.class, historicByteArrayId[0])).isNull();

      return null;
    });

    // when
    asyncThread.makeContinue();
    asyncThread.waitUntilDone();

    // then
    if (testRule.isOptimisticLockingExceptionSuppressible()) {
      assertThat(runtimeService.createVariableInstanceQuery().singleResult().getName()).isEqualTo(VARIABLE_NAME);
      assertThat(new String((byte[]) runtimeService.createVariableInstanceQuery().singleResult().getValue())).isEqualTo(ANOTHER_VARIABLE_VALUE);
    } else {
      // with CockroachDB, the OLE can't be ignored, the TX will fail and be rolled-back
      assertThat(asyncThread.getException()).isInstanceOf(CrdbTransactionRetryException.class);
    }
  }

  public class AsyncThread extends ControllableCommand<Void> {

    String processInstanceId;
    String historicByteArrayId;

    AsyncThread(String processInstanceId, String historicByteArrayId) {
      this.processInstanceId = processInstanceId;
      this.historicByteArrayId = historicByteArrayId;
    }

    public Void execute(CommandContext commandContext) {
      commandContext.getDbEntityManager()
        .selectById(ByteArrayEntity.class, historicByteArrayId); // cache

      monitor.sync();

      runtimeService.setVariable(processInstanceId, VARIABLE_NAME,
        Variables.byteArrayValue(ANOTHER_VARIABLE_VALUE.getBytes()));

      return null;
    }

  }

}
