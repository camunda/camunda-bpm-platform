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

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.TransactionContext;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionLogger;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * Base class for handling the context of a transaction. Provides template
 * methods for handling transaction contexts.
 *
 * @author Daniel Meyer
 */
public abstract class AbstractTransactionContext implements TransactionContext {

  public final static TransactionLogger LOG = ProcessEngineLogger.TX_LOGGER;

  @Override
  public void commit() {
    // managed transaction, ignore
  }

  @Override
  public void rollback() {
    // managed transaction, mark rollback-only if not done so already.
    try {
      doRollback();
    } catch (Exception e) {
      throw LOG.exceptionWhileInteractingWithTransaction("setting transaction rollback only", e);
    }
  }

  @Override
  public void addTransactionListener(TransactionState transactionState, final TransactionListener transactionListener) {
    CommandContext commandContext = Context.getCommandContext();
    try {
      addTransactionListener(transactionState, transactionListener, commandContext);
    } catch (Exception e) {
      throw LOG.exceptionWhileInteractingWithTransaction("registering synchronization", e);
    }
  }

  public abstract static class TransactionStateSynchronization {

    protected final TransactionListener transactionListener;
    protected final TransactionState transactionState;
    private final CommandContext commandContext;

    public TransactionStateSynchronization(TransactionState transactionState, TransactionListener transactionListener, CommandContext commandContext) {
      this.transactionState = transactionState;
      this.transactionListener = transactionListener;
      this.commandContext = commandContext;
    }

    public void beforeCompletion() {
      if(TransactionState.COMMITTING.equals(transactionState)
         || TransactionState.ROLLINGBACK.equals(transactionState)) {
        transactionListener.execute(commandContext);
      }
    }

    public void afterCompletion(int status) {
      if(isRolledBack(status) && TransactionState.ROLLED_BACK.equals(transactionState)) {
        transactionListener.execute(commandContext);
      } else if(isCommitted(status) && TransactionState.COMMITTED.equals(transactionState)) {
        transactionListener.execute(commandContext);
      }
    }

    protected abstract boolean isCommitted(int status);

    protected abstract boolean isRolledBack(int status);

  }

  @Override
  public boolean isTransactionActive() {
    try {
      return isTransactionActiveInternal();
    } catch (Exception e) {
      throw LOG.exceptionWhileInteractingWithTransaction("getting transaction state", e);
    }
  }

  protected abstract void doRollback() throws Exception;
  protected abstract boolean isTransactionActiveInternal() throws Exception;
  protected abstract void addTransactionListener(TransactionState transactionState, TransactionListener transactionListener, CommandContext commandContext) throws Exception;
}
