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

import org.camunda.bpm.client.exception.BadRequestException;
import org.camunda.bpm.client.exception.ConnectionLostException;
import org.camunda.bpm.client.exception.EngineException;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.UnknownHttpErrorException;
import org.camunda.bpm.client.task.ExternalTask;

import java.util.List;

/**
 * <p>Provides a way to define a back off between fetch and lock requests.
 *
 * <p>Note: Since an implementation of this interface may be executed by multiple threads,
 * it is recommended to implement the custom backoff strategy in a thread-safe manner.
 */
public interface ErrorAwareBackoffStrategy extends BackoffStrategy {

  /**
   * This is to provide compatibility with existing BackoffStrategy configurations.
   * Do not override. Implementations of ErrorAwareBackoffStrategy should override
   * {@link #reconfigure(List, ExternalTaskClientException)} instead.
   *
   * @param externalTasks which have been fetched
   */
  @Override
  default void reconfigure(List<ExternalTask> externalTasks) {
    reconfigure(externalTasks, null);
  }

  /**
   * <p>Reconfigures the back off strategy based on the fetched external tasks and any error that
   * might have occurred. It is invoked before {@link #calculateBackoffTime}.
   *
   * <p>The implementation might count the amount of invocations and realize a strategy reset.
   *
   * @param externalTasks which have been fetched
   * @param exception can be of the following types: <ul>
   *    <li>{@link EngineException} if something went wrong during the engine execution (e.g., a persistence exception occurred).
   *    <li>{@link BadRequestException} if an illegal operation was performed or the given data is invalid.
   *    <li>{@link ConnectionLostException} if the connection could not be established.
   *    <li>{@link UnknownHttpErrorException} if the HTTP status code is not known by the client.
   */
  void reconfigure(List<ExternalTask> externalTasks, ExternalTaskClientException exception);

  /**
   * <p>Calculates the back off time and is invoked after {@link #reconfigure(List, ExternalTaskClientException)}.
   *
   * @return the back off time between fetch and lock requests in milliseconds
   */
  long calculateBackoffTime();
}
