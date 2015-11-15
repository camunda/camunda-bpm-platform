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
package org.camunda.bpm.engine.impl.interceptor;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;

/**
 * Intercepts {@link OptimisticLockingException} and tries to run the
 * same command again. The number of retries and the time waited between retries
 * is configurable.
 *
 * @author Daniel Meyer
 */
public class RetryInterceptor extends CommandInterceptor {

  private final static CommandLogger LOG = CommandLogger.CMD_LOGGER;

  protected int numOfRetries = 3;
  protected int waitTimeInMs = 50;
  protected int waitIncreaseFactor = 5;

  public <T> T execute(Command<T> command) {
    long waitTime=waitTimeInMs;
    int failedAttempts=0;

    do {
      if (failedAttempts > 0) {
        LOG.debugWaitingFor(waitTime);
        waitBeforeRetry(waitTime);
        waitTime *= waitIncreaseFactor;
      }

      try {

        // try to execute the command
        return next.execute(command);

      } catch (OptimisticLockingException e) {
        LOG.debugCaughtOptimisticLockingException(e);
      }

      failedAttempts ++;
    } while(failedAttempts<=numOfRetries);

    throw new ProcessEngineException(numOfRetries + " retries failed with OptimisticLockingException. Giving up.");
  }

  protected void waitBeforeRetry(long waitTime) {
    try {
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // ignore
    }
  }

  public void setNumOfRetries(int numOfRetries) {
    this.numOfRetries = numOfRetries;
  }

  public void setWaitIncreaseFactor(int waitIncreaseFactor) {
    this.waitIncreaseFactor = waitIncreaseFactor;
  }

  public void setWaitTimeInMs(int waitTimeInMs) {
    this.waitTimeInMs = waitTimeInMs;
  }

  public int getNumOfRetries() {
    return numOfRetries;
  }

  public int getWaitIncreaseFactor() {
    return waitIncreaseFactor;
  }

  public int getWaitTimeInMs() {
    return waitTimeInMs;
  }
}
