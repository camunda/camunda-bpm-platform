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
package org.camunda.bpm.engine.impl.cfg.jta;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * JTA-based implementation of the {@link AbstractTransactionContext}
 *
 * @author Daniel Meyer
 */
public class JtaTransactionContext extends AbstractTransactionContext {

  protected final TransactionManager transactionManager;

  public JtaTransactionContext(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  @Override
  protected void doRollback() throws Exception {
    // managed transaction, mark rollback-only if not done so already
    Transaction transaction = getTransaction();
    int status = transaction.getStatus();
    if (status != Status.STATUS_NO_TRANSACTION && status != Status.STATUS_ROLLEDBACK) {
      transaction.setRollbackOnly();
    }
  }

  @Override
  protected void addTransactionListener(TransactionState transactionState, final TransactionListener transactionListener, CommandContext commandContext) throws Exception{
    getTransaction().registerSynchronization(new JtaTransactionStateSynchronization(transactionState, transactionListener, commandContext));
  }

  protected Transaction getTransaction() {
    try {
      return transactionManager.getTransaction();
    } catch (Exception e) {
      throw LOG.exceptionWhileInteractingWithTransaction("getting transaction", e);
    }
  }

  @Override
  protected boolean isTransactionActiveInternal() throws Exception {
    return transactionManager.getStatus() != Status.STATUS_MARKED_ROLLBACK && transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION;
  }

  public static class JtaTransactionStateSynchronization extends TransactionStateSynchronization implements Synchronization {

    public JtaTransactionStateSynchronization(TransactionState transactionState, TransactionListener transactionListener, CommandContext commandContext) {
      super(transactionState, transactionListener, commandContext);
    }

    @Override
    protected boolean isRolledBack(int status) {
      return Status.STATUS_ROLLEDBACK == status;
    }

    @Override
    protected boolean isCommitted(int status) {
      return Status.STATUS_COMMITTED == status;
    }

  }
}
