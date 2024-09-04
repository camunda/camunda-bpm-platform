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

import io.smallrye.config.WithDefault;
import java.util.Map;

public interface CamundaJobExecutorConfig {

  /**
   * The Camunda JobExecutor configuration properties. For more details,
   * @see <a href="https://docs.camunda.org/manual/latest/reference/deployment-descriptors/tags/job-executor/#job-acquisition-configuration-properties">Job-Acquisition Configuration Properties</a>
   */
  Map<String, String> genericConfig();

  /**
   * The Camunda JobExecutor thread pool config. This thread pool is responsible for running
   * Camunda jobs.
   */
  ThreadPoolConfig threadPool();

  interface ThreadPoolConfig {
    /**
     * Sets the maximum number of threads that can be present in the Quarkus-managed
     * thread pool for the Camunda JobExecutor. The default value is 10.
     */
    @WithDefault("10")
    int maxPoolSize();

    /**
     * Sets the size of the Quarkus-managed JobExecutor queue. The default value is 3.
     */
    @WithDefault("3")
    int queueSize();

  }
}