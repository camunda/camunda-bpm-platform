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
package org.camunda.bpm.client.backoff;

import org.camunda.bpm.client.task.ExternalTask;

import java.util.List;

/**
 * <p>Provides an exponential backoff strategy.
 *
 * @author Nikola Koevski
 */
public class ExponentialBackoffStrategy implements BackoffStrategy {

  protected long initTime;
  protected float factor;
  protected int level;
  protected long maxTime;

  public ExponentialBackoffStrategy() {
    this(500L, 2, 60000L);
  }

  /**
   * @param initTime in milliseconds for which the client is suspended after the first request
   * @param factor is the base of the power by which the waiting time increases
   * @param maxTime in milliseconds for which the client can be suspended
   */
  public ExponentialBackoffStrategy(long initTime, float factor, long maxTime) {
    this.initTime = initTime;
    this.factor = factor;
    this.level = 0;
    this.maxTime = maxTime;
  }

  @Override
  public void reconfigure(List<ExternalTask> externalTasks) {
    if (externalTasks.isEmpty()) {
      level++;
    } else {
      level = 0;
    }
  }

  @Override
  public long calculateBackoffTime() {
    if (level == 0) {
      return 0L;
    }

    long backoffTime = (long) (initTime * Math.pow(factor, level - 1));
    return Math.min(backoffTime, maxTime);
  }
}
