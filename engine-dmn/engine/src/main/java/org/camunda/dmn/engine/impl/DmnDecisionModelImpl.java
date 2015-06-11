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

package org.camunda.dmn.engine.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.context.DmnDecisionContext;
import org.camunda.dmn.engine.DmnDecisionModel;
import org.camunda.dmn.engine.DmnDecisionResult;

public class DmnDecisionModelImpl implements DmnDecisionModel {

  protected Map<String, DmnDecision> decisions = new HashMap<String, DmnDecision>();

  public List<DmnDecision> getDecisions() {
    return new ArrayList<DmnDecision>(decisions.values());
  }

  public DmnDecision getDecision(String decisionId) {
    return decisions.get(decisionId);
  }

  public void addDecision(DmnDecision decision) {
    decisions.put(decision.getId(), decision);
  }

  public DmnDecisionResult evaluate(String decisionId, DmnDecisionContext decisionContext) {
    return getDecision(decisionId).evaluate(decisionContext);
  }

}
