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
package org.camunda.bpm.quarkus.engine.extension;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class CamundaJobExecutorConfig {

  /**
   * The Camunda JobExecutor configuration properties. For more details,
   * @see <a href="https://docs.camunda.org/manual/latest/reference/deployment-descriptors/tags/job-executor/#job-acquisition-configuration-properties">Job-Acquisition Configuration Properties</a>
   */
  @ConfigItem(name = ConfigItem.PARENT)
  public Map<String, String> genericConfig;

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
     * thread pool for the Camunda JobExecutor. The default value is 10.
     */
    @ConfigItem(defaultValue = "10")
    public int maxPoolSize;

    /**
     * Sets the size of the Quarkus-managed JobExecutor queue. The default value is 3.
     */
    @ConfigItem(defaultValue = "3")
    public int queueSize;

  }
}