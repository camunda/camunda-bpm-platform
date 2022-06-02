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
package org.camunda.bpm.engine.spring;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Dave Syer
 * @author Tom Baeyens
 */
public class SpringTransactionInterceptor extends CommandInterceptor {

  protected PlatformTransactionManager transactionManager;
  protected int transactionPropagation;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  /**
   * This constructor doesn't pass an instance of the {@link ProcessEngineConfigurationImpl} class.
   * As a result, if it is used with CockroachDB, concurrency conflicts that occur on transaction
   * commit will not be handled by the process engine.
   *
   * @deprecated use the {@link #SpringTransactionInterceptor(PlatformTransactionManager, int, ProcessEngineConfigurationImpl)}
   *    constructor to ensure that when used with CockroachDB, concurrency conflicts that occur
   *    on transaction commit are detected and handled.
   */
  @Deprecated
  public SpringTransactionInterceptor(PlatformTransactionManager transactionManager, int transactionPropagation) {
    this(transactionManager, transactionPropagation, null);
  }

  public SpringTransactionInterceptor(PlatformTransactionManager transactionManager,
                                      int transactionPropagation,
                                      ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.transactionManager = transactionManager;
    this.transactionPropagation = transactionPropagation;
    this.processEngineConfiguration = processEngineConfiguration;
  }

  @SuppressWarnings("unchecked")
  public <T> T execute(final Command<T> command) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(transactionPropagation);
    try {
      // don't use lambdas here => CAM-12810
      return (T) transactionTemplate.execute((TransactionCallback) status -> next.execute(command));
    } catch (TransactionSystemException ex) {
      // When CockroachDB is used, a CRDB concurrency error may occur on transaction commit.
      // To ensure that these errors are still detected as OLEs, we must catch them and wrap
      // them in a CrdbTransactionRetryException
      SQLException sqlException = (SQLException) ex.getCause();
      if (processEngineConfiguration != null
          && DbSqlSession.isCrdbConcurrencyConflictOnCommit(sqlException, processEngineConfiguration)) {
        throw ProcessEngineLogger.PERSISTENCE_LOGGER.crdbTransactionRetryExceptionOnCommit(ex);
      } else {
        throw ex;
      }
    }
  }
}