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
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;

/**
 * @author Guillaume Nodet
 */
public class JtaTransactionInterceptor extends CommandInterceptor {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final TransactionManager transactionManager;
  protected final boolean requiresNew;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  public JtaTransactionInterceptor(TransactionManager transactionManager,
                                   boolean requiresNew,
                                   ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.transactionManager = transactionManager;
    this.requiresNew = requiresNew;
    this.processEngineConfiguration = processEngineConfiguration;
  }

  public <T> T execute(Command<T> command) {
    Transaction oldTx = null;
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

  private void doBegin() {
    try {
      transactionManager.begin();
    } catch (NotSupportedException e) {
      throw new TransactionException("Unable to begin transaction", e);
    } catch (SystemException e) {
      throw new TransactionException("Unable to begin transaction", e);
    }
  }

  private boolean isExisting() {
    try {
      return transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION;
    } catch (SystemException e) {
      throw new TransactionException("Unable to retrieve transaction status", e);
    }
  }

  private Transaction doSuspend() {
    try {
      return transactionManager.suspend();
    } catch (SystemException e) {
      throw new TransactionException("Unable to suspend transaction", e);
    }
  }

  private void doResume(Transaction tx) {
    if (tx != null) {
      try {
        transactionManager.resume(tx);
      } catch (SystemException e) {
        throw new TransactionException("Unable to resume transaction", e);
      } catch (InvalidTransactionException e) {
        throw new TransactionException("Unable to resume transaction", e);
      }
    }
  }

  private void doCommit() {
    try {
      transactionManager.commit();
    } catch (HeuristicMixedException e) {
      throw new TransactionException("Unable to commit transaction", e);
    } catch (HeuristicRollbackException e) {
      throw new TransactionException("Unable to commit transaction", e);
    } catch (RollbackException e) {
      // When CockroachDB is used, a CRDB concurrency error may occur on transaction commit.
      // To ensure that these errors are still detected as OLEs, we must catch them and wrap
      // them in a CrdbTransactionRetryException
      if (DbSqlSession.isCrdbConcurrencyConflictOnCommit(e, processEngineConfiguration)) {
        throw ProcessEngineLogger.PERSISTENCE_LOGGER.crdbTransactionRetryExceptionOnCommit(e);
      } else {
        throw new TransactionException("Unable to commit transaction", e);
      }
    } catch (SystemException e) {
      throw new TransactionException("Unable to commit transaction", e);
    } catch (RuntimeException e) {
      doRollback(true);
      throw e;
    } catch (Error e) {
      doRollback(true);
      throw e;
    }
  }

  private void doRollback(boolean isNew) {
    try {
      if (isNew) {
        transactionManager.rollback();
      } else {
        transactionManager.setRollbackOnly();
      }
    } catch (SystemException e) {
      LOG.exceptionWhileRollingBackTransaction(e);
    }
  }

  public static class TransactionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private TransactionException() {
    }

    private TransactionException(String s) {
      super(s);
    }

    private TransactionException(String s, Throwable throwable) {
      super(s, throwable);
    }

    private TransactionException(Throwable throwable) {
      super(throwable);
    }
  }
}
