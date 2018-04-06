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

import org.camunda.bpm.client.topic.impl.TopicSubscriptionManager;

/**
 * <p>The ClientBackOffStrategy provides a way to define the wait time between requests to the server.</p>
 *
 * @author Nikola Koevski
 */
public interface ClientBackOffStrategy {

  /**
   * Is invoked if the client receives no external tasks for the current topic subscriptions
   */
  void startWaiting();

  /**
   * Is invoked when a request to the server returns a non-empty list of external tasks. This method
   * is used to reset the back off strategy to its starting state.
   */
  void reset();

  /**
   * Is invoked before stopping the task acquisition thread in the {@link TopicSubscriptionManager#stop()} method
   */
  void stopWaiting();
}
