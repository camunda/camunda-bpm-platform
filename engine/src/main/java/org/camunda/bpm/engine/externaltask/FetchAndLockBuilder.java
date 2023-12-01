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
package org.camunda.bpm.engine.externaltask;

/**
 * Fetch And Lock Builder used to enable a Fluent API that exposes all parameters for fetch and Lock operation.
 */
public interface FetchAndLockBuilder {

  /**
   * Configures the workerId that will be used during the Fetch and Lock operation.
   *
   * @param workerId the given workerId
   * @return the builder
   */
  FetchAndLockBuilder workerId(String workerId);

  /**
   * Configures the max tasks fetching that will be used during the Fetch and Lock operation.
   *
   * @param maxTasks the given number of max tasks
   * @return the builder
   */
  FetchAndLockBuilder maxTasks(int maxTasks);

  /**
   * Configures fetching to consider (or not) priority during the Fetch and Lock operation.
   *
   * @param usePriority the given usePriority flag. If true, tasks will be fetched in descending order.
   * @return the builder
   */
  FetchAndLockBuilder usePriority(boolean usePriority);

  /**
   * Configures the fetching during the Fetch and Lock Operation to include ordering by create time of the external tasks.
   * This method can be combined by calling asc() or desc() afterwards to define an ascending or descending order respectively.
   *
   * @return the builder
   */
  FetchAndLockBuilder orderByCreateTime();

  /**
   * Configures the order to be ascending.
   *
   * @return the builder
   */
  FetchAndLockBuilder asc();

  /**
   * Configures the order to be descending.
   *
   * @return the builder
   */
  FetchAndLockBuilder desc();

  /**
   * Returns the {@link ExternalTaskQueryTopicBuilder} to handle all the configuration that applies per topic.
   *
   * @return the builder
   */
  ExternalTaskQueryTopicBuilder subscribe();
}
