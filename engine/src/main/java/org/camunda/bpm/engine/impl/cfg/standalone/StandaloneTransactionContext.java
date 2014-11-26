/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.cfg.standalone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.camunda.bpm.engine.impl.cfg.TransactionContext;
import org.camunda.bpm.engine.impl.cfg.TransactionListener;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.PersistenceSession;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Sebastian Menski
 */
public class StandaloneTransactionContext implements TransactionContext {

  private static Logger log = Logger.getLogger(StandaloneTransactionContext.class.getName());
  protected CommandContext commandContext;
  protected Map<TransactionState, List<TransactionListener>> stateTransactionListeners = null;

  public StandaloneTransactionContext(CommandContext commandContext) {
    this.commandContext = commandContext;
  }

  public void addTransactionListener(TransactionState transactionState, TransactionListener transactionListener) {
    if (stateTransactionListeners==null) {
      stateTransactionListeners = new HashMap<TransactionState, List<TransactionListener>>();
    }
    List<TransactionListener> transactionListeners = stateTransactionListeners.get(transactionState);
    if (transactionListeners==null) {
      transactionListeners = new ArrayList<TransactionListener>();
      stateTransactionListeners.put(transactionState, transactionListeners);
    }
    transactionListeners.add(transactionListener);
  }

  public void commit() {
    log.fine("firing event committing...");
    fireTransactionEvent(TransactionState.COMMITTING);
    log.fine("committing the persistence session...");
    getPersistenceProvider().commit();
    log.fine("firing event committed...");
    fireTransactionEvent(TransactionState.COMMITTED);
  }

  protected void fireTransactionEvent(TransactionState transactionState) {
    if (stateTransactionListeners==null) {
      return;
    }
    List<TransactionListener> transactionListeners = stateTransactionListeners.get(transactionState);
    if (transactionListeners==null) {
      return;
    }
    for (TransactionListener transactionListener: transactionListeners) {
      transactionListener.execute(commandContext);
    }
  }

  private PersistenceSession getPersistenceProvider() {
    return commandContext.getSession(PersistenceSession.class);
  }

  public void rollback() {
    try {
      try {
        log.fine("firing event rolling back...");
        fireTransactionEvent(TransactionState.ROLLINGBACK);

      } catch (Throwable exception) {
        log.info("Exception during transaction: " + exception.getMessage());
        Context.getCommandInvocationContext().trySetThrowable(exception);
      } finally {
        log.fine("rolling back persistence session...");
        getPersistenceProvider().rollback();
      }

    } catch (Throwable exception) {

      log.info("Exception during transaction: " + exception.getMessage());
      Context.getCommandInvocationContext().trySetThrowable(exception);

    } finally {
      log.fine("firing event rolled back...");
      fireTransactionEvent(TransactionState.ROLLED_BACK);
    }
  }
}
