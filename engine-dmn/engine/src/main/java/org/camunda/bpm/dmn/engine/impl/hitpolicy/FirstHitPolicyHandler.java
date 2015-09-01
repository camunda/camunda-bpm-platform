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

package org.camunda.bpm.dmn.engine.impl.hitpolicy;

import java.util.Collections;

import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableResultImpl;

public class FirstHitPolicyHandler implements DmnHitPolicyHandler {

  public DmnDecisionTableResult apply(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    if (!decisionTableResult.getMatchingRules().isEmpty()) {
      DmnDecisionTableRule firstMatchedRule = decisionTableResult.getMatchingRules().get(0);
      ((DmnDecisionTableResultImpl) decisionTableResult).setMatchingRules(Collections.singletonList(firstMatchedRule));
    }
    return decisionTableResult;
  }

}
