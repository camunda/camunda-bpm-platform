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
import java.util.Collection;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLogicEvaluationEvent;

public class DmnDecisionEvaluationEventImpl implements DmnDecisionEvaluationEvent {

  protected DmnDecisionLogicEvaluationEvent decisionResult;
  protected Collection<DmnDecisionLogicEvaluationEvent> requiredDecisionResults = new ArrayList<DmnDecisionLogicEvaluationEvent>();
  protected long executedDecisionInstances;
  protected long executedDecisionElements;

  @Override
  public DmnDecisionLogicEvaluationEvent getDecisionResult() {
    return decisionResult;
  }

  public void setDecisionResult(DmnDecisionLogicEvaluationEvent decisionResult) {
    this.decisionResult = decisionResult;
  }

  @Override
  public Collection<DmnDecisionLogicEvaluationEvent> getRequiredDecisionResults() {
    return requiredDecisionResults;
  }

  public void setRequiredDecisionResults(Collection<DmnDecisionLogicEvaluationEvent> requiredDecisionResults) {
    this.requiredDecisionResults = requiredDecisionResults;
  }

  @Override
  public long getExecutedDecisionInstances() {
    return executedDecisionInstances;
  }

  public void setExecutedDecisionInstances(long executedDecisionInstances) {
    this.executedDecisionInstances = executedDecisionInstances;
  }

  @Override
  public long getExecutedDecisionElements() {
    return executedDecisionElements;
  }

  public void setExecutedDecisionElements(long executedDecisionElements) {
    this.executedDecisionElements = executedDecisionElements;
  }

  @Override
  public String toString() {
    DmnDecision dmnDecision = decisionResult.getDecision();
    return "DmnDecisionEvaluationEventImpl{" +
      " key="+ dmnDecision.getKey() +
      ", name="+ dmnDecision.getName() +
      ", decisionLogic=" + dmnDecision.getDecisionLogic() +
      ", requiredDecisionResults=" + requiredDecisionResults +
      ", executedDecisionInstances=" + executedDecisionInstances +
      ", executedDecisionElements=" + executedDecisionElements +
      '}';
  }

}
