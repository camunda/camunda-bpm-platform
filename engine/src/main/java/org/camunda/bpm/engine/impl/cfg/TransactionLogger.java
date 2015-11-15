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
package org.camunda.bpm.engine.impl.cfg;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * @author Daniel Meyer
 *
 */
public class TransactionLogger extends ProcessEngineLogger {

  public ProcessEngineException exceptionWhileInteractingWithTransaction(String operation, Throwable e) {
    throw new ProcessEngineException(exceptionMessage(
        "001",
        "{} while {}",
        e.getClass().getSimpleName(), operation), e);
  }

  public void debugTransactionOperation(String string) {
    logDebug("002",
        string);
  }

  public void exceptionWhileFiringEvent(TransactionState state, Throwable exception) {
    logError("003",
        "Exception while firing event {}: {}", state, exception.getMessage(), exception);
  }

  public void debugFiringEventRolledBack() {
    logDebug("004", "Firing event rolled back");
  }

}
