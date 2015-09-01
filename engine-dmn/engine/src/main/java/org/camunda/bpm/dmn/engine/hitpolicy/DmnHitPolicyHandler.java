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

package org.camunda.bpm.dmn.engine.hitpolicy;

import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.DmnHitPolicyException;

public interface DmnHitPolicyHandler {

  /**
   * Applies hit policy. Depending on the hit policy this can mean filtering and sorting of matching rules or
   * aggregating results.
   *
   * @param decisionTable the evaluated decision table
   * @param decisionTableResult the full evaluation result
   * @return the final evaluation result
   * @throws DmnHitPolicyException if the hit policy cannot be applied to the decision outputs
   */
  DmnDecisionTableResult apply(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult);

}
