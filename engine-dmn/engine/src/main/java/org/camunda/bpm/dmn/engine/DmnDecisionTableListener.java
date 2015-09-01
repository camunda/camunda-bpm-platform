/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.dmn.engine;

/**
 * A listener which will be notified after a decision table
 * was evaluated.
 */
public interface DmnDecisionTableListener {

  /**
   * Will be called after a decision table was evaluated.
   *
   * @param decisionTable the evaluated decision table
   * @param decisionTableResult the result of the decision table evaluation
   */
  void notify(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult);

}
