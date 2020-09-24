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
package org.camunda.bpm.engine.spring.test.transaction.crdb;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This tests simulates a CockroachDB concurrency error retry scenario, where the transaction
 * is managed by the Process Engine. Concurrency errors involved in external transaction
 * management setups are not tested since they should be handled by the app developer.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:org/camunda/bpm/engine/spring/test/transaction/" +
    "CrdbTransactionIntegrationTest-applicationContext.xml"})
public class CrdbTransactionIntegrationTest {

  @Rule
  @Autowired
  public ProcessEngineRule rule;

  @Autowired
  public ProcessEngineConfigurationImpl processEngineConfiguration;

  @Autowired
  HistoryService historyService;

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/spring/test/transaction/" +
          "CrdbTransactionIntegrationTest.simpleProcess.bpmn20.xml" })
  public void shouldRetryEngineManagedTransaction() {
    // given
    // a custom, retryable Command that starts a Process Instance of a simple process
    // with a delegate, and fails (only) on the first try with a CrdbTransactionRetryException.

    // when
    // the command is executed
    processEngineConfiguration
      .getCommandExecutorTxRequired()
      .execute(new Command<Void>() {
        @Override
        public Void execute(CommandContext commandContext) {

          // and a simple process is started with a failing delegate
          commandContext.getProcessEngineConfiguration().getRuntimeService()
              .startProcessInstanceByKey("simpleProcess");

          return null;
        }

        @Override
        public boolean isRetryable() {
          // ensure that the command is retryable
          return true;
        }
      });

    // then
    // a new transaction is used on the second, successful command invocation,
    // and only one simple process is committed to the database
    long historicProcessInstanceCount =  historyService.createHistoricProcessInstanceQuery()
        .processDefinitionKey("simpleProcess")
        .count();
    assertThat(historicProcessInstanceCount).isEqualTo(1L);
    assertThat(CrdbConcurrencyConflictDelegate.getTransactions().size()).isEqualTo(2);
  }

}