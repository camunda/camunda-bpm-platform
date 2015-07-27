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

package org.camunda.bpm.dmn.engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnClauseEntry;
import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContext;

public class DmnRuleImpl extends DmnElementImpl implements DmnRule {

  protected List<DmnClauseEntry> conditions = new ArrayList<DmnClauseEntry>();
  protected List<DmnClauseEntry> conclusions = new ArrayList<DmnClauseEntry>();

  public List<DmnClauseEntry> getConditions() {
    return conditions;
  }

  public void setConditions(List<DmnClauseEntry> conditions) {
    this.conditions = conditions;
  }

  public void addCondition(DmnClauseEntry condition) {
    conditions.add(condition);
  }

  public List<DmnClauseEntry> getConclusions() {
    return conclusions;
  }

  public void setConclusions(List<DmnClauseEntry> conclusions) {
    this.conclusions = conclusions;
  }

  public void addConclusion(DmnClauseEntry conclusion) {
    conclusions.add(conclusion);
  }

  public boolean isApplicable(DmnDecisionContext decisionContext) {
    return decisionContext.isApplicable(this);
  }

  public DmnDecisionOutput getOutput(DmnDecisionContext decisionContext) {
    return decisionContext.getOutput(this);
  }

  public String toString() {
    return "DmnRuleImpl{" +
      "key='" + key + '\'' +
      ", name='" + name + '\'' +
      ", conditions=" + conditions +
      ", conclusions=" + conclusions +
      "} ";
  }

}
