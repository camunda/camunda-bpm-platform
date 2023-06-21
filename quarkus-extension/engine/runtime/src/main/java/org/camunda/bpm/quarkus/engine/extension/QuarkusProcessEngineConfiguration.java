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
package org.camunda.bpm.quarkus.engine.extension;

import org.camunda.bpm.engine.cdi.CdiJtaProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.CommandContextInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandCounterInterceptor;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.JakartaTransactionInterceptor;
import org.camunda.bpm.engine.impl.interceptor.LogInterceptor;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;

import jakarta.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.List;

public class QuarkusProcessEngineConfiguration extends CdiJtaProcessEngineConfiguration {

  /**
   * Default values.
   */
  public QuarkusProcessEngineConfiguration() {
    setJobExecutorActivate(true);

    setJdbcUrl(null);
    setJdbcUsername(null);
    setJdbcPassword(null);
    setJdbcDriver(null);
    setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_TRUE); // automatic schema update
    setTransactionsExternallyManaged(true);

    setIdGenerator(new StrongUuidGenerator());

    setHistory(HistoryLevel.HISTORY_LEVEL_FULL.getName()); // Cockpit needs it
  }

  /**
   * We need to make sure, that the root command always calls {@link TransactionManager#begin} in its interceptor chain
   * since Agroal does not support deferred/lazy enlistment. This is why we override this method to add
   * the {@link JakartaTransactionInterceptor} to the interceptor chain.
   */
  @Override
  protected void initCommandExecutorDbSchemaOperations() {
    if (commandExecutorSchemaOperations == null) {
      List<CommandInterceptor> commandInterceptorsDbSchemaOperations = new ArrayList<>();
      commandInterceptorsDbSchemaOperations.add(new LogInterceptor());
      commandInterceptorsDbSchemaOperations.add(new CommandCounterInterceptor(this));
      commandInterceptorsDbSchemaOperations.add(new JakartaTransactionInterceptor(transactionManager, false, this));
      commandInterceptorsDbSchemaOperations.add(new CommandContextInterceptor(dbSchemaOperationsCommandContextFactory, this));
      commandInterceptorsDbSchemaOperations.add(actualCommandExecutor);
      commandExecutorSchemaOperations = initInterceptorChain(commandInterceptorsDbSchemaOperations);
    }
  }

}
