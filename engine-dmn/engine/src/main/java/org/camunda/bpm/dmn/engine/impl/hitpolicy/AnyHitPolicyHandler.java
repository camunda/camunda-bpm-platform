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
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.dmn.engine.DmnDecisionTableValue;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;

public class AnyHitPolicyHandler implements DmnHitPolicyHandler {

  public static final DmnHitPolicyLogger LOG = DmnLogger.HIT_POLICY_LOGGER;

  public DmnDecisionTableResult apply(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    List<DmnDecisionTableRule> matchingRules = decisionTableResult.getMatchingRules();

    if (!matchingRules.isEmpty()) {
      if (allOutputsAreEqual(matchingRules)) {
        DmnDecisionTableRule firstMatchingRule = matchingRules.get(0);
        ((DmnDecisionTableResultImpl) decisionTableResult).setMatchingRules(Collections.singletonList(firstMatchingRule));
      } else {
        throw LOG.anyHitPolicyRequiresThatAllOutputsAreEqual(matchingRules);
      }
    }

    return decisionTableResult;
  }

  protected boolean allOutputsAreEqual(List<DmnDecisionTableRule> matchingRules) {
    Map<String, DmnDecisionTableValue> firstOutputs = matchingRules.get(0).getOutputs();
    if (firstOutputs == null) {
      for (int i = 1; i < matchingRules.size(); i++) {
        if (matchingRules.get(i).getOutputs() != null) {
          return false;
        }
      }
    } else {
      for (int i = 1; i < matchingRules.size(); i++) {
        if (!firstOutputs.equals(matchingRules.get(i).getOutputs())) {
          return false;
        }
      }
    }
    return true;
  }

}
