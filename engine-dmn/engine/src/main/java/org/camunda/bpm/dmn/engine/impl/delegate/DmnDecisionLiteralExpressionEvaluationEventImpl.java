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

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLiteralExpressionEvaluationEvent;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class DmnDecisionLiteralExpressionEvaluationEventImpl implements DmnDecisionLiteralExpressionEvaluationEvent {

  protected DmnDecision decision;

  protected String outputName;
  protected TypedValue outputValue;

  protected long executedDecisionElements;

  public DmnDecision getDecision() {
    return decision;
  }

  public void setDecision(DmnDecision decision) {
    this.decision = decision;
  }

  public String getOutputName() {
    return outputName;
  }

  public void setOutputName(String outputName) {
    this.outputName = outputName;
  }

  public TypedValue getOutputValue() {
    return outputValue;
  }

  public void setOutputValue(TypedValue outputValue) {
    this.outputValue = outputValue;
  }

  public long getExecutedDecisionElements() {
    return executedDecisionElements;
  }

  public void setExecutedDecisionElements(long executedDecisionElements) {
    this.executedDecisionElements = executedDecisionElements;
  }

  @Override
  public String toString() {
    return "DmnDecisionLiteralExpressionEvaluationEventImpl [" +
        " key="+ decision.getKey() +
        ", name="+ decision.getName() +
        ", decisionLogic=" + decision.getDecisionLogic() +
        ", outputName=" + outputName +
        ", outputValue=" + outputValue +
        ", executedDecisionElements=" + executedDecisionElements +
        "]";
  }



}
