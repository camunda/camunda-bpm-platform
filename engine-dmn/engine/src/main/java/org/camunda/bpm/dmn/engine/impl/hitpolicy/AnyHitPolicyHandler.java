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

import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.bpm.model.dmn.HitPolicy;

public class AnyHitPolicyHandler extends AbstractDmnHitPolicyHandler {

  public static final HitPolicy HIT_POLICY = HitPolicy.ANY;

  public HitPolicy getHandledHitPolicy() {
    return HIT_POLICY;
  }

  public boolean handlesHitPolicy(HitPolicy hitPolicy) {
    return HIT_POLICY.equals(hitPolicy);
  }

  public List<DmnRule> filterMatchingRules(DmnDecisionTable decisionTable, List<DmnRule> matchingRules) {
    return matchingRules;
  }

  @Override
  public DmnDecisionResult getDecisionResult(DmnDecisionTable decisionTable, List<DmnDecisionOutput> decisionOutputs) {
    DmnDecisionResult decisionResult = new DmnDecisionResultImpl();
    if (!decisionOutputs.isEmpty()) {
      DmnDecisionOutput firstDecisionOutput = decisionOutputs.get(0);
      if (allOutputsAreEqual(decisionOutputs)) {
        decisionResult.add(firstDecisionOutput);
      }
      else {
        throw LOG.anyHitPolicyRequiresThatAllOutputsAreEqual(decisionOutputs);
      }
    }
    return decisionResult;
  }

  protected boolean allOutputsAreEqual(List<DmnDecisionOutput> decisionOutputs) {
    DmnDecisionOutput firstDecisionOutput = decisionOutputs.get(0);
    if (firstDecisionOutput == null) {
      for (int i = 1; i < decisionOutputs.size(); i++) {
        if (decisionOutputs.get(i) != null) {
          return false;
        }
      }
    }
    else {
      for (int i = 1; i < decisionOutputs.size(); i++) {
        if (!firstDecisionOutput.equals(decisionOutputs.get(i))) {
          return false;
        }
      }
    }
    return true;
  }

}
