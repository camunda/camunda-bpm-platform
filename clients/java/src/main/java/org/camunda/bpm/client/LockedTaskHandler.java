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
 * <p>Interface for a custom implementation of the handler, which is invoked for each fetched and locked task</p>
 *
 * @author Tassilo Weidner
 */
public interface LockedTaskHandler {

  /**
   * Has been executed for each fetched and locked task
   *
   * @param lockedTask the context is represented of
   */
  void execute(LockedTask lockedTask);

}
