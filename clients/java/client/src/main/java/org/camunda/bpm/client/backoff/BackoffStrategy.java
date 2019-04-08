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
 * <p>Provides a way to define a back off between fetch and lock requests.
 *
 * <p>Note: Since an implementation of this interface may be executed by multiple threads,
 * it is recommended to implement the custom backoff strategy in a thread-safe manner.
 *
 * @author Nikola Koevski
 */
public interface BackoffStrategy {

  /**
   * <p>Reconfigures the back off strategy based on the fetched external tasks and is invoked
   * before {@link #calculateBackoffTime}.
   *
   * <p>The implementation might count the amount of invocations and realize a strategy reset.
   *
   * @param externalTasks which have been fetched
   */
  void reconfigure(List<ExternalTask> externalTasks);

  /**
   * <p>Calculates the back off time and is invoked after {@link #reconfigure(List)}.
   *
   * @return the back off time between fetch and lock requests in milliseconds
   */
  long calculateBackoffTime();
}
