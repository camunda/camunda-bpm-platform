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
package org.camunda.bpm.client.spring.annotation;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.spring.impl.PostProcessorConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Annotation to bootstrap the External Task Client
 *
 * <p>Heads-up: <ul><li>for attributes of type {@link String}, the string <strong>$null$</strong>
 * is reserved  and used as default value
 * <li>for attributes of type {@link Long}, the value {@link Long#MIN_VALUE} is reserved and used as
 * default value
 * <li>for attributes of type {@link Integer}, the value {@link Integer#MIN_VALUE} is reserved and
 * used as default value
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Import(PostProcessorConfiguration.class)
public @interface EnableExternalTaskClient {

  String STRING_NULL_VALUE = "$null$";
  long LONG_NULL_VALUE = Long.MIN_VALUE;
  int INT_NULL_VALUE = Integer.MIN_VALUE;

  String STRING_ORDER_BY_ASC_VALUE = "asc";
  String STRING_ORDER_BY_DESC_VALUE = "desc";

  /**
   * Base url of the Camunda Runtime Platform REST API. This information is mandatory.
   * Alias of {@link #baseUrl()}.
   *
   * @return baseUrl of the Camunda Runtime Platform REST API
   */
  @AliasFor("baseUrl")
  String value() default STRING_NULL_VALUE;

  /**
   * Base url of the Camunda Runtime Platform REST API. This information is mandatory.
   * Alias of {@link #value()}.
   *
   * @return baseUrl of the Camunda Runtime Platform REST API
   */
  @AliasFor("value")
  String baseUrl() default STRING_NULL_VALUE;

  /**
   * Specifies the maximum amount of tasks that can be fetched within one request.
   * This information is optional. Default is 10.
   *
   * @return maxTasks which are supposed to be fetched within one request
   */
  int maxTasks() default INT_NULL_VALUE;

  /**
   * A custom worker id the Workflow Engine is aware of. This information is optional.
   * Note: make sure to choose a unique worker id
   * <p>
   * If not given or null, a worker id is generated automatically which consists of the
   * hostname as well as a random and unique 128 bit string (UUID).
   *
   * @return workerId the Workflow Engine is aware of
   */
  String workerId() default STRING_NULL_VALUE;

  /**
   * Specifies whether tasks should be fetched based on their priority or arbitrarily.
   * This information is optional. Default is <code>true</code>.
   *
   * @return usePriority when fetching and locking tasks
   */
  boolean usePriority() default true;

  /**
   * Specifies whether tasks should be fetched based on their createTime.
   * This is optional. The default is <code>false</code>.
   *
   * @return useCreateTime when fetching and locking tasks
   */
  boolean useCreateTime() default false;

  /**
   * Specifies whether tasks should be fetched based on their createTime with a configured order ()
   * This information is optional.
   *
   * @return useCreateTime when fetching and locking tasks
   */
  String orderByCreateTime() default STRING_NULL_VALUE;

  /**
   * Asynchronous response (long polling) is enabled if a timeout is given.
   * Specifies the maximum waiting time for the response of fetched and locked external tasks.
   * The response is performed immediately, if external tasks are available in the moment of
   * the request. This information is optional. Unless a timeout is given, fetch and lock
   * responses are synchronous.
   *
   * @return asyncResponseTimeout of fetched and locked external tasks in milliseconds
   */
  long asyncResponseTimeout() default LONG_NULL_VALUE;

  /**
   * Disables immediate fetching for external tasks after bootstrapping the client.
   * To start fetching {@link ExternalTaskClient#start()} must be called.
   *
   * @return disableAutoFetching when after bootstrapping the client
   */
  boolean disableAutoFetching() default false;

  /**
   * Disables the client-side backoff strategy. When set to {@code true},
   * a {@link BackoffStrategy} bean is ignored.
   * <p>
   * NOTE: Please bear in mind that disabling the client-side backoff can lead to heavy
   * load situations on engine side. To avoid this, please specify an appropriate
   * {@link #asyncResponseTimeout()}.
   *
   * @return disableBackoffStrategy
   */
  boolean disableBackoffStrategy() default false;

  /**
   * @return lockDuration <ul>
   * <li> in milliseconds to lock the external tasks
   * <li> must be greater than zero
   * <li> the default lock duration is 20 seconds (20,000 milliseconds)
   * <li> is overridden by the lock duration configured on a topic subscription
   * </ul>
   */
  long lockDuration() default LONG_NULL_VALUE;

  /**
   * Specifies the date format to de-/serialize date variables.
   * The option defaults to <code>yyyy-MM-dd'T'HH:mm:ss.SSSZ</code>
   *
   * @return dateFormat date format to be used
   */
  String dateFormat() default STRING_NULL_VALUE;

  /**
   * Specifies the serialization format that is used to serialize objects when no specific
   * format is requested. This option defaults to application/json.
   *
   * @return defaultSerializationFormat serialization format to be used
   */
  String defaultSerializationFormat() default STRING_NULL_VALUE;

}
