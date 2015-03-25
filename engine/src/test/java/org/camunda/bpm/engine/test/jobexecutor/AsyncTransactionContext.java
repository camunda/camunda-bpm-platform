/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.jobexecutor;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.cfg.standalone.StandaloneTransactionContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Daniel Meyer
 *
 */
public class AsyncTransactionContext extends StandaloneTransactionContext {

  public AsyncTransactionContext(CommandContext commandContext) {
    super(commandContext);
  }

  // event is fired in new thread
  protected void fireTransactionEvent(final TransactionState transactionState) {
    Thread thread = new Thread() {
      public void run() {
        fireTransactionEventAsync(transactionState);
      }
    };

    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      throw new ProcessEngineException(e);
    }
  }

  protected void fireTransactionEventAsync(final TransactionState transactionState) {
    super.fireTransactionEvent(transactionState);
  }



}
