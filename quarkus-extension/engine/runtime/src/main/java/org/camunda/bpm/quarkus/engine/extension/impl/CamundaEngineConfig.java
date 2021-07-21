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
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME, name = "camunda.bpm")
public class CamundaEngineConfig {

  /**
   * The Camunda JobExecutor thread pool config. This thread pool is responsible for running
   * Camunda jobs.
   */
  @ConfigItem
  public ThreadPoolConfig threadPool;

  @ConfigGroup
  public static class ThreadPoolConfig {
    /**
     * The size of the thread pool assigned to the Quarkus-managed JobExecutor.
     *
     * Default value is 10.
     */
    @ConfigItem(defaultValue = "10")
    public int size;

    /**
     * The size of the Quarkus-managed JobExecutor queue.
     *
     * Default value is 3.
     */
    @ConfigItem(defaultValue = "3")
    public int queueSize;

  }
}