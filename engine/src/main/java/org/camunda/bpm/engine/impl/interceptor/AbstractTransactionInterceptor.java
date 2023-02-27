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
package org.camunda.bpm.engine.impl.interceptor;

import java.lang.reflect.UndeclaredThrowableException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;

/**
 * Base interceptor class for handling transactions for a command. Provides a
 * general template method to handle the transaction-based calls for a command
 * execution.
 */
public abstract class AbstractTransactionInterceptor extends CommandInterceptor {

  protected final boolean requiresNew;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public AbstractTransactionInterceptor(boolean requiresNew,
      ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.requiresNew = requiresNew;
    this.processEngineConfiguration = processEngineConfiguration;
  }

  @Override
  public <T> T execute(Command<T> command) {
    Object oldTx = null;
    try {
      boolean existing = isExisting();
      boolean isNew = !existing || requiresNew;
      if (existing && requiresNew) {
        oldTx = doSuspend();
      }
      if (isNew) {
        doBegin();
      }
      T result;
      try {
        result = next.execute(command);
      } catch (RuntimeException ex) {
        doRollback(isNew);
        throw ex;
      } catch (Error err) {
        doRollback(isNew);
        throw err;
      } catch (Exception ex) {
        doRollback(isNew);
        throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
      }
      if (isNew) {
        doCommit();
      }
      return result;
    } finally {
      doResume(oldTx);
    }
  }

  protected void handleRollbackException(Exception rollbackException) {
    // When CockroachDB is used, a CRDB concurrency error may occur on transaction commit.
    // To ensure that these errors are still detected as OLEs, we must catch them and wrap
    // them in a CrdbTransactionRetryException
    if (DbSqlSession.isCrdbConcurrencyConflictOnCommit(rollbackException, processEngineConfiguration)) {
      throw ProcessEngineLogger.PERSISTENCE_LOGGER.crdbTransactionRetryExceptionOnCommit(rollbackException);
    } else {
      throw new TransactionException("Unable to commit transaction", rollbackException);
    }
  }

  protected abstract void doResume(Object oldTx);
  protected abstract void doCommit();
  protected abstract void doRollback(boolean isNew);
  protected abstract void doBegin();
  protected abstract Object doSuspend();
  protected abstract boolean isExisting();
}
