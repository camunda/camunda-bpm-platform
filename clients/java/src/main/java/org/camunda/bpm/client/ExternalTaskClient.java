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
package org.camunda.bpm.client;

import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;

/**
 * <p>Camunda external task client</p>
 *
 * @author Tassilo Weidner
 */
public interface ExternalTaskClient {

  /**
   * Creates a fluent builder to configure the Camunda client
   *
   * @return builder to apply configurations on
   */
  public static ExternalTaskClientBuilder create() {
    return new ExternalTaskClientBuilderImpl();
  }

  /**
   * Creates a fluent builder to create and configure a topic subscription
   *
   * @param topicName the worker subscribes to
   * @return builder to apply configurations on
   */
  public TopicSubscriptionBuilder subscribe(String topicName);

  /**
   * Stops continuous fetching and locking of tasks
   */
  public void shutdown();

}
