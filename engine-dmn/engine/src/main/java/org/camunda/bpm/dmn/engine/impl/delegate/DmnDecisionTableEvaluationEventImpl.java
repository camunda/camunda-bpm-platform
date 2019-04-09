/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.dmn.engine.impl.delegate;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedInput;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnDecisionTableEvaluationEventImpl implements DmnDecisionTableEvaluationEvent {

  protected DmnDecision decision;
  protected List<DmnEvaluatedInput> inputs = new ArrayList<DmnEvaluatedInput>();
  protected List<DmnEvaluatedDecisionRule> matchingRules = new ArrayList<DmnEvaluatedDecisionRule>();
  protected String collectResultName;
  protected TypedValue collectResultValue;
  protected long executedDecisionElements;

  public DmnDecision getDecisionTable() {
    return getDecision();
  }

  public DmnDecision getDecision() {
    return decision;
  }

  public void setDecisionTable(DmnDecision decision) {
   this.decision = decision;
  }

  public List<DmnEvaluatedInput> getInputs() {
    return inputs;
  }

  public void setInputs(List<DmnEvaluatedInput> inputs) {
    this.inputs = inputs;
  }

  public List<DmnEvaluatedDecisionRule> getMatchingRules() {
    return matchingRules;
  }

  public void setMatchingRules(List<DmnEvaluatedDecisionRule> matchingRules) {
    this.matchingRules = matchingRules;
  }

  public String getCollectResultName() {
    return collectResultName;
  }

  public void setCollectResultName(String collectResultName) {
    this.collectResultName = collectResultName;
  }

  public TypedValue getCollectResultValue() {
    return collectResultValue;
  }

  public void setCollectResultValue(TypedValue collectResultValue) {
    this.collectResultValue = collectResultValue;
  }

  public long getExecutedDecisionElements() {
    return executedDecisionElements;
  }

  public void setExecutedDecisionElements(long executedDecisionElements) {
    this.executedDecisionElements = executedDecisionElements;
  }

  @Override
  public String toString() {
    return "DmnDecisionTableEvaluationEventImpl{" +
      " key="+ decision.getKey() +
      ", name="+ decision.getName() +
      ", decisionLogic=" + decision.getDecisionLogic() +
      ", inputs=" + inputs +
      ", matchingRules=" + matchingRules +
      ", collectResultName='" + collectResultName + '\'' +
      ", collectResultValue=" + collectResultValue +
      ", executedDecisionElements=" + executedDecisionElements +
      '}';
  }

}
