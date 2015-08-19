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

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.model.dmn.HitPolicy;

public class OutputOrderHitPolicyHandler extends AbstractDmnHitPolicyHandler {

  public static final HitPolicy HIT_POLICY = HitPolicy.OUTPUT_ORDER;

  public HitPolicy getHandledHitPolicy() {
    return HIT_POLICY;
  }

  public boolean handlesHitPolicy(HitPolicy hitPolicy) {
    return HIT_POLICY.equals(hitPolicy);
  }

  public List<DmnRule> filterMatchingRules(DmnDecisionTable decisionTable, List<DmnRule> matchingRules) {
    // TODO: implement output ordering
    return matchingRules;
  }

}
