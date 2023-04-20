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

/**
 * JTA-based implementation of the {@link AbstractTransactionInterceptor}
 *
 * @author Guillaume Nodet
 */
public class JtaTransactionInterceptor extends AbstractTransactionInterceptor {

  protected final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final TransactionManager transactionManager;

  public JtaTransactionInterceptor(TransactionManager transactionManager,
                                   boolean requiresNew,
                                   ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(requiresNew, processEngineConfiguration);
    this.transactionManager = transactionManager;
  }

  @Override
  protected void doBegin() {
    try {
      transactionManager.begin();
    } catch (NotSupportedException e) {
      throw new TransactionException("Unable to begin transaction", e);
    } catch (SystemException e) {
      throw new TransactionException("Unable to begin transaction", e);
    }
  }

  @Override
  protected boolean isExisting() {
    try {
      return transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION;
    } catch (SystemException e) {
      throw new TransactionException("Unable to retrieve transaction status", e);
    }
  }

  @Override
  protected Transaction doSuspend() {
    try {
      return transactionManager.suspend();
    } catch (SystemException e) {
      throw new TransactionException("Unable to suspend transaction", e);
    }
  }

  @Override
  protected void doResume(Object tx) {
    if (tx != null) {
      try {
        transactionManager.resume((Transaction) tx);
      } catch (SystemException e) {
        throw new TransactionException("Unable to resume transaction", e);
      } catch (InvalidTransactionException e) {
        throw new TransactionException("Unable to resume transaction", e);
      }
    }
  }

  @Override
  protected void doCommit() {
    try {
      transactionManager.commit();
    } catch (HeuristicMixedException e) {
      throw new TransactionException("Unable to commit transaction", e);
    } catch (HeuristicRollbackException e) {
      throw new TransactionException("Unable to commit transaction", e);
    } catch (RollbackException e) {
      handleRollbackException(e);
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

  @Override
  protected void doRollback(boolean isNew) {
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
}
