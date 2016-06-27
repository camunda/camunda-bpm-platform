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

package org.camunda.bpm.dmn.engine.impl.delegate;

import java.util.ArrayList;
import java.util.Collection;

import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;

public class DmnDecisionEvaluationEventImpl implements DmnDecisionEvaluationEvent {

  protected DmnDecisionTableEvaluationEvent decisionResult;
  protected Collection<DmnDecisionTableEvaluationEvent> requiredDecisions = new ArrayList<DmnDecisionTableEvaluationEvent>();
  protected long evaluatedDecisions;

  public DmnDecisionTableEvaluationEvent getDecisionResult() {
    return decisionResult;  
  }

  public void setDecisionResult(DmnDecisionTableEvaluationEvent decisionResult) {
    this.decisionResult = decisionResult;
  }

  public Collection<DmnDecisionTableEvaluationEvent> getRequiredDecisions() {
    return requiredDecisions;
  }

  public void setRequiredDecisions(Collection<DmnDecisionTableEvaluationEvent> requiredDecisions) {
    this.requiredDecisions = requiredDecisions;
  }

  public long getEvaluatedDecisions() {
    return evaluatedDecisions;
  }

  public void setEvaluatedDecisions(long evaluatedDecisions) {
    this.evaluatedDecisions = evaluatedDecisions;
  }

  @Override
  public String toString() {
    DmnDecisionImpl dmnDecision = ((DmnDecisionImpl)decisionResult.getDecisionTable());
    return "DmnDecisionTableEvaluationEventImpl{" +
      " key="+ dmnDecision.getKey() +
      ", name="+ dmnDecision.getName() + 
      ", decisionTable=" + dmnDecision.getRelatedDecisionTable() +
      ", requiredDecisions=" + requiredDecisions +
      ", evaluatedDecisions=" + evaluatedDecisions +
      '}';
  }

}
