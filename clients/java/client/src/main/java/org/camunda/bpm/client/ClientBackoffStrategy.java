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

/**
 * <p>The ClientBackoffStrategy provides a way to define a back off between fetch and lock requests.</p>
 *
 * @author Nikola Koevski
 */
public interface ClientBackoffStrategy {

  /**
   * Is invoked when no external tasks have been received for the current topic subscriptions.
   * The implementation might realize a back off between fetch and lock requests.
   */
  void suspend();

  /**
   * Is invoked when at least one external task has been received for the current topic subscriptions.
   * The implementation might realize a reset of the back off to its starting state.
   */
  void reset();

  /**
   * Is invoked to interrupt the suspension when:
   * <ul>
   *   <li> a new topic subscription has been added
   *   <li> the client has been stopped
   * </ul>
   *
   * The implementation might interrupt the back off.
   */
  void resume();
}
