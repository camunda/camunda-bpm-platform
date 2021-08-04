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
package org.camunda.bpm.quarkus.engine.extension.impl;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class CamundaJobExecutorConfig {

  /**
   * Sets the maximal number of jobs to be acquired at once. The default value is 3.
   */
  @ConfigItem(defaultValue = "3")
  public int maxJobsPerAcquisition;

  /**
   * Specifies the time in milliseconds an acquired job is locked for execution. During that time,
   * no other job executor can acquire the job. The default value is 300000.
   */
  @ConfigItem(defaultValue = "300000")
  public int lockTimeInMillis;

  /**
   * Specifies the wait time of the job acquisition thread in milliseconds in case there are less
   * jobs available for execution than requested during acquisition. If this is repeatedly the
   * case, the wait time is increased exponentially by the factor <code>waitIncreaseFactor</code>.
   * The wait time is capped by <code>maxWait</code>. The default value is 5000.
   */
  @ConfigItem(defaultValue = "5000")
  public int waitTimeInMillis;

  /**
   * Specifies the maximum wait time of the job acquisition thread in milliseconds in case there
   * are less jobs available for execution than requested during acquisition. The default value
   * is 60000.
   */
  @ConfigItem(defaultValue = "60000")
  public long maxWait;

  /**
   * Specifies the wait time of the job acquisition thread in milliseconds in case jobs were
   * acquired but could not be locked. This condition indicates that there are other job
   * acquisition threads acquiring jobs in parallel. If this is repeatedly the case, the backoff
   * time is increased exponentially by the factor waitIncreaseFactor. The time is capped by
   * <code>maxBackoff</code>. With every increase in backoff time, the number of jobs acquired
   * increases by <code>waitIncreaseFactor</code> as well. The default value is 0.
   */
  @ConfigItem(defaultValue = "0")
  public int backoffTimeInMillis;

  /**
   * Specifies the maximum wait time of the job acquisition thread in milliseconds in case jobs
   * were acquired but could not be locked. The default value is 0.
   */
  @ConfigItem(defaultValue = "0")
  public long maxBackoff;

  /**
   * Specifies the number of successful job acquisition cycles without a job locking failure
   * before the backoff time is decreased again. In that case, the backoff time is reduced by
   * <code>waitIncreaseFactor</code>. The default value is 100.
   */
  @ConfigItem(defaultValue = "100")
  public int backoffDecreaseThreshold;

  /**
   * Specifies the factor by which wait and backoff time are increased in case their activation
   * conditions are repeatedly met. The default value is 2.
   */
  @ConfigItem(defaultValue = "2")
  public float waitIncreaseFactor;

  /**
   * The Camunda JobExecutor thread pool config. This thread pool is responsible for running
   * Camunda jobs.
   */
  @ConfigItem
  public ThreadPoolConfig threadPool;

  @ConfigGroup
  public static class ThreadPoolConfig {
    /**
     * Sets the maximum number of threads that can be present in the Quarkus-managed
     * thread pool for the Camunda JobExecutor. The default value is -1, i.e. unlimited.
     */
    @ConfigItem(defaultValue = "-1")
    public int maxPoolSize;

    /**
     * Sets the size of the Quarkus-managed JobExecutor queue. The default value is 3.
     */
    @ConfigItem(defaultValue = "3")
    public int queueSize;

  }
}