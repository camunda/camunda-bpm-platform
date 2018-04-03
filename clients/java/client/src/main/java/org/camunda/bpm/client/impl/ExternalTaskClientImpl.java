/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.client.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.impl.variable.VariableMappers;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.impl.RequestInterceptorHandler;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.camunda.bpm.client.topic.impl.TopicSubscriptionBuilderImpl;
import org.camunda.bpm.client.topic.impl.TopicSubscriptionManager;

import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class ExternalTaskClientImpl implements ExternalTaskClient {

  protected TopicSubscriptionManager topicSubscriptionManager;
  protected RequestInterceptorHandler requestInterceptorHandler;

  protected ExternalTaskClientImpl(ExternalTaskClientBuilderImpl clientBuilder) {
    String workerId = clientBuilder.getWorkerId();
    String baseUrl = clientBuilder.getBaseUrl();

    List<ClientRequestInterceptor> interceptors = clientBuilder.getInterceptors();
    requestInterceptorHandler = new RequestInterceptorHandler(interceptors);

    ObjectMapper objectMapper = initObjectMapper();
    VariableMappers variableMappers = new VariableMappers(objectMapper);
    EngineClient engineClient = new EngineClient(workerId, baseUrl, requestInterceptorHandler, variableMappers, objectMapper);
    topicSubscriptionManager = new TopicSubscriptionManager(engineClient, variableMappers);
  }

  public TopicSubscriptionBuilder subscribe(String topicName) {
    return new TopicSubscriptionBuilderImpl(topicName, topicSubscriptionManager);
  }

  public void stop() {
    topicSubscriptionManager.stop();
  }

  public TopicSubscriptionManager getTopicSubscriptionManager() {
    return topicSubscriptionManager;
  }

  public RequestInterceptorHandler getRequestInterceptorHandler() {
    return requestInterceptorHandler;
  }

  protected ObjectMapper initObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);

    return objectMapper;
  }

}
