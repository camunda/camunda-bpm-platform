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
package org.camunda.bpm.client.impl;

import org.camunda.bpm.client.impl.engineclient.EngineClientException;

/**
 * @author Tassilo Weidner
 */
public class WorkerManagerLogger extends ClientLogger {

  public void exceptionWhilePerformingFetchAndLock(EngineClientException e) {
    logError(
      "001", "Exception while fetch and lock tasks '{}'", e);
  }

  public void exceptionWhileExecutingLockedTaskHandler(Throwable e) {
    logError(
      "002", "Exception while executing locked task handler '{}'", e);
  }

  public void exceptionWhileSuspending(InterruptedException e) {
    logError(
      "003", "Exception while suspending '{}'", e);
  }

  public void exceptionWhileShuttingDown(InterruptedException e) {
    logError(
      "004", "Exception while shutting down '{}'", e);
  }


}
