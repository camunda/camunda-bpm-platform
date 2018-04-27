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

package org.camunda.bpm.client.backoff.impl;

import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.task.ExternalTask;

import java.util.List;

/**
 * A simple implementation of the exponential org.camunda.bpm.client.backoff algorithm for a org.camunda.bpm.client.backoff strategy.
 *
 * @author Nikola Koevski
 */
public class ExponentialBackoffStrategy implements BackoffStrategy {

  protected long startingBackOffTime;
  protected float backOffFactor;
  protected int backOffLevel = 0;
  protected long maxBackOffTime;

  public ExponentialBackoffStrategy() {
    this(500L, 2, 60000L);
  }

  /**
   * @param startingBackOffTime is the starting org.camunda.bpm.client.backoff time
   * @param backOffFactor is the base of the power by which the waiting time increases.
   * @param maxBackOffTime is the  amount of time for which the client can be suspended.
   */
  public ExponentialBackoffStrategy(long startingBackOffTime, float backOffFactor, long maxBackOffTime) {
    this.startingBackOffTime = startingBackOffTime;
    this.backOffFactor = backOffFactor;
    this.maxBackOffTime = maxBackOffTime;
  }

  @Override
  public void reconfigure(List<ExternalTask> externalTasks) {
    if (externalTasks.isEmpty()) {
      backOffLevel++;
    } else {
      backOffLevel = 0;
    }
  }

  @Override
  public long calculateBackoffTime() {
    long backoffTime;
    if (backOffLevel == 0) {
      return 0L;
    } else {
      backoffTime = (long) (startingBackOffTime * Math.pow(backOffFactor, backOffLevel - 1));
    }

    return Math.min(backoffTime, maxBackOffTime);
  }
}
