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
package org.camunda.bpm.client;

import java.util.function.Consumer;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;

/**
 * <p>A fluent builder to configure the Camunda client</p>
 *
 * @author Tassilo Weidner
 */
public interface ExternalTaskClientBuilder {

  /**
   * Base url of the Camunda BPM Platform REST API. This information is mandatory.
   * <p>
   * If this method is used, it will create a permanent url resolver with the given baseUrl.
   *
   * @param baseUrl of the Camunda BPM Platform REST API
   * @return the builder
   */
  ExternalTaskClientBuilder baseUrl(String baseUrl);

  /**
   * Url resolver of the Camunda 7 REST API. This information is mandatory.
   * <p>
   * If the server is in a cluster or you are using spring cloud, you can create a class which implements UrlResolver..
   * <p>
   * this is a sample for spring cloud DiscoveryClient
   * <pre>
   * {@code
   * public class CustomUrlResolver implements UrlResolver{
   * protected String serviceId;
   *
   * protected DiscoveryClient discoveryClient;
   *
   *   protected String getRandomServiceInstance() {
   *     List serviceInstances = discoveryClient.getInstances(serviceId);
   *     Random random = new Random();
   *
   *     return serviceInstances.get(random.nextInt(serviceInstances.size())).getUri().toString();
   *   }
   *
   *   public String getBaseUrl() {
   *     return getRandomServiceInstance();
   *   }
   * }
   * </pre>
   * @param urlResolver of the Camunda 7 REST API
   * @return the builder
   */
  ExternalTaskClientBuilder urlResolver(UrlResolver urlResolver);

  /**
   * A custom worker id the Workflow Engine is aware of. This information is optional.
   * Note: make sure to choose a unique worker id
   * <p>
   * If not given or null, a worker id is generated automatically which consists of the
   * hostname as well as a random and unique 128 bit string (UUID).
   *
   * @param workerId the Workflow Engine is aware of
   * @return the builder
   */
  ExternalTaskClientBuilder workerId(String workerId);

  /**
   * Adds an interceptor to change a request before it is sent to the http server.
   * This information is optional.
   *
   * @param interceptor which changes the request
   * @return the builder
   */
  ExternalTaskClientBuilder addInterceptor(ClientRequestInterceptor interceptor);

  /**
   * Specifies the maximum amount of tasks that can be fetched within one request.
   * This information is optional. Default is 10.
   *
   * @param maxTasks which are supposed to be fetched within one request
   * @return the builder
   */
  ExternalTaskClientBuilder maxTasks(int maxTasks);

  /**
   * Specifies whether tasks should be fetched based on their priority or arbitrarily.
   * This information is optional. Default is <code>true</code>.
   *
   * @param usePriority when fetching and locking tasks
   * @return the builder
   */
  ExternalTaskClientBuilder usePriority(boolean usePriority);

  /**
   * Specifies whether tasks should be fetched based on their create time.
   * If useCreateTime is passed using <code>true</code>, the tasks are going to be fetched in Descending order,
   * otherwise <code>false</code> will not consider create time.
   *
   * @param useCreateTime the flag to control whether create time should be considered
   *                      as a desc sorting criterion (newer tasks will be returned first)
   * @return the builder
   */
  ExternalTaskClientBuilder useCreateTime(boolean useCreateTime);

  /**
   * Fluent API method for configuring createTime as a sorting criterion for fetching of the tasks. Can be used in
   * conjunction with asc or desc methods to configure the respective order for createTime.
   * The method needs to be called first before specifying the order.
   *
   * @return the builder
   */
  ExternalTaskClientBuilder orderByCreateTime();

  /**
   * Fluent API method to configure Ascending order on the associated field. Used in conjunction with an orderBy method
   * such as orderByCreateTime.
   *
   * @return the builder
   */
  ExternalTaskClientBuilder asc();

  /**
   * Fluent API method to configure Descending order on the associated field. Used in conjunction with an orderBy method
   * such as orderByCreateTime.
   *
   * @return the builder
   */
  ExternalTaskClientBuilder desc();

  /**
   * Specifies the serialization format that is used to serialize objects when no specific
   * format is requested. This option defaults to application/json.
   *
   * @param defaultSerializationFormat serialization format to be used
   * @return the builder
   */
  ExternalTaskClientBuilder defaultSerializationFormat(String defaultSerializationFormat);

  /**
   * Specifies the date format to de-/serialize date variables.
   *
   * @param dateFormat date format to be used
   * @return the builder
   */
  ExternalTaskClientBuilder dateFormat(String dateFormat);

  /**
   * Asynchronous response (long polling) is enabled if a timeout is given.
   * Specifies the maximum waiting time for the response of fetched and locked external tasks.
   * The response is performed immediately, if external tasks are available in the moment of the request.
   * This information is optional. Unless a timeout is given, fetch and lock responses are synchronous.
   *
   * @param asyncResponseTimeout of fetched and locked external tasks in milliseconds
   * @return the builder
   */
  ExternalTaskClientBuilder asyncResponseTimeout(long asyncResponseTimeout);

  /**
   * @param lockDuration <ul>
   *                     <li> in milliseconds to lock the external tasks
   *                     <li> must be greater than zero
   *                     <li> the default lock duration is 20 seconds (20,000 milliseconds)
   *                     <li> is overridden by the lock duration configured on a topic subscription
   *                     </ul>
   * @return the builder
   */
  ExternalTaskClientBuilder lockDuration(long lockDuration);

  /**
   * Disables immediate fetching for external tasks after calling {@link #build} to bootstrap the client.
   * To start fetching {@link ExternalTaskClient#start()} must be called.
   *
   * @return the builder
   */
  ExternalTaskClientBuilder disableAutoFetching();

  /**
   * Adds a custom strategy to the client for defining the org.camunda.bpm.client.backoff between two requests.
   * This information is optional. By default {@link ExponentialBackoffStrategy} is applied.
   *
   * @param backoffStrategy which realizes a custom org.camunda.bpm.client.backoff strategy
   * @return the builder
   */
  ExternalTaskClientBuilder backoffStrategy(BackoffStrategy backoffStrategy);

  /**
   * Disables the client-side backoff strategy. On invocation, the configuration option {@link #backoffStrategy} is ignored.
   * <p>
   * NOTE: Please bear in mind that disabling the client-side backoff can lead to heavy load situations on engine side.
   * To avoid this, please specify an appropriate {@link #asyncResponseTimeout(long)}.
   *
   * @return the builder
   */
  ExternalTaskClientBuilder disableBackoffStrategy();

  /**
   * Exposes the internal Apache {@link HttpClientBuilder} for custom client configurations.
   * <p>
   * Interceptors added via {@link #addInterceptor(ClientRequestInterceptor)} are added as last in the {@link #build()} method.
   *
   * @param httpClientConsumer the parameter that accepts the {@link HttpClientBuilder}
   * @return the builder
   */
  ExternalTaskClientBuilder customizeHttpClient(Consumer<HttpClientBuilder> httpClientConsumer);

  /**
   * Bootstraps the Camunda client
   *
   * @return the builder
   * @throws ExternalTaskClientException <ul>
   *                                       <li> if base url is null or string is empty
   *                                       <li> if hostname cannot be retrieved
   *                                       <li> if maximum amount of tasks is not greater than zero
   *                                       <li> if maximum asynchronous response timeout is not greater than zero
   *                                       <li> if lock duration is not greater than zero
   *                                     </ul>
   */
  ExternalTaskClient build();

}
