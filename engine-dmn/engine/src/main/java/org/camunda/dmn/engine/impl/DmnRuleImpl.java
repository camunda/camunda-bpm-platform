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
import java.util.Map;
import java.util.TreeMap;

import org.camunda.dmn.engine.DmnDecisionOutput;
import org.camunda.dmn.engine.DmnExpression;
import org.camunda.dmn.engine.DmnRule;
import org.camunda.dmn.engine.context.DmnDecisionContext;

public class DmnRuleImpl implements DmnRule {

  protected String id;
  protected Map<String, DmnExpression> inputExpressions = new TreeMap<String, DmnExpression>();
  protected Map<String, List<DmnExpression>> conditions = new TreeMap<String, List<DmnExpression>>();
  protected List<DmnExpression> conclusions = new ArrayList<DmnExpression>();

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setInputExpressions(Map<String, DmnExpression> inputExpressions) {
    this.inputExpressions = inputExpressions;
  }

  public void addInputExpression(String clauseId, DmnExpression inputExpression) {
    inputExpressions.put(clauseId, inputExpression);
  }

  public Map<String, DmnExpression> getInputExpressions() {
    return inputExpressions;
  }

  public void setConditions(Map<String, List<DmnExpression>> conditions) {
    this.conditions = conditions;
  }

  public Map<String, List<DmnExpression>> getConditions() {
    return conditions;
  }

  public void setConclusions(List<DmnExpression> conclusions) {
    this.conclusions = conclusions;
  }

  public List<DmnExpression> getConclusions() {
    return conclusions;
  }

  public boolean isApplicable(DmnDecisionContext decisionContext) {
    return decisionContext.isApplicable(this);
  }

  public DmnDecisionOutput getOutput(DmnDecisionContext decisionContext) {
    return decisionContext.getOutput(this);
  }

}
