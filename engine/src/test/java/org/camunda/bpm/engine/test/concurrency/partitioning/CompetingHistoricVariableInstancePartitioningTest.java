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

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.variable.Variables;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Tassilo Weidner
 */

public class CompetingHistoricVariableInstancePartitioningTest extends AbstractPartitioningTest {

  final protected String VARIABLE_NAME = "aVariableName";
  final protected String VARIABLE_VALUE = "aVariableValue";
  final protected String ANOTHER_VARIABLE_VALUE = "anotherVariableValue";

  public void testConcurrentFetchAndDelete() {
    // given
    String processInstanceId = deployAndStartProcess(PROCESS_WITH_USERTASK,
      Variables.createVariables().putValue(VARIABLE_NAME, VARIABLE_VALUE)).getId();

    ThreadControl asyncThread = executeControllableCommand(new AsyncThread(processInstanceId));

    asyncThread.waitForSync();

    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        HistoricVariableInstanceEntity historicVariableInstanceEntity =
          (HistoricVariableInstanceEntity) historyService.createHistoricVariableInstanceQuery().singleResult();

        commandContext.getDbEntityManager().delete(historicVariableInstanceEntity);

        return null;
      }
    });

    // assume
    assertThat(historyService.createHistoricVariableInstanceQuery().singleResult(), nullValue());

    // when
    asyncThread.makeContinue();
    asyncThread.waitUntilDone();

    // then
    assertThat(runtimeService.createVariableInstanceQuery().singleResult().getName(), is(VARIABLE_NAME));
    assertThat((String) runtimeService.createVariableInstanceQuery().singleResult().getValue(), is(ANOTHER_VARIABLE_VALUE));
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
