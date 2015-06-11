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
import java.util.List;

import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnRule;
import org.camunda.dmn.engine.context.DmnDecisionContext;
import org.camunda.dmn.engine.DmnDecisionResult;

public class DmnDecisionImpl implements DmnDecision {

  protected String decisionId;
  protected List<DmnRule> rules = new ArrayList<DmnRule>();

  public DmnDecisionImpl(String decisionId) {
    this.decisionId = decisionId;
  }

  public String getId() {
    return decisionId;
  }

  public void setRules(List<DmnRule> rules) {
    this.rules = rules;
  }

  public List<DmnRule> getRules() {
    return rules;
  }

  public void addRule(DmnRule rule) {
    rules.add(rule);
  }

  public DmnDecisionResult evaluate(DmnDecisionContext decisionContext) {
    return decisionContext.evaluate(this);
  }

}
