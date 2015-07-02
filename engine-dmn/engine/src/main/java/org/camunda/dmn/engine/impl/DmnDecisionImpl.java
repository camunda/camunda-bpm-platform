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
import java.util.TreeMap;

import org.camunda.dmn.engine.DmnDecision;
import org.camunda.dmn.engine.DmnDecisionResult;
import org.camunda.dmn.engine.DmnExpression;
import org.camunda.dmn.engine.DmnRule;
import org.camunda.dmn.engine.context.DmnDecisionContext;

public class DmnDecisionImpl implements DmnDecision {

  protected String id;
  protected Map<String, DmnExpression> inputExpressions = new TreeMap<String, DmnExpression>();
  protected Map<String, DmnExpression> inputEntries = new HashMap<String, DmnExpression>();
  protected Map<String, DmnExpression> outputEntries = new HashMap<String, DmnExpression>();
  protected List<DmnRule> rules = new ArrayList<DmnRule>();

  public DmnDecisionImpl() {
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setInputExpressions(Map<String, DmnExpression> inputExpressions) {
    this.inputExpressions = inputExpressions;
  }

  public Map<String, DmnExpression> getInputExpressions() {
    return inputExpressions;
  }

  public void addInputExpression(String clauseId, DmnExpression inputExpression) {
    inputExpressions.put(clauseId, inputExpression);
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

  public void addInputEntry(DmnExpression inputEntry) {
    inputEntries.put(inputEntry.getId(), inputEntry);
  }

  public DmnExpression getInputEntry(String id) {
    return inputEntries.get(id);
  }

  public void addOutputEntry(DmnExpression outputEntry) {
    outputEntries.put(outputEntry.getId(), outputEntry);
  }

  public DmnExpression getOutputEntry(String id) {
    return outputEntries.get(id);
  }

}
