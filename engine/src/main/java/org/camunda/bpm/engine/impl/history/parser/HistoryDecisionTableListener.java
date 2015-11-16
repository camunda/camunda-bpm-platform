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

package org.camunda.bpm.engine.impl.history.parser;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationListener;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.CoreExecutionContext;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.DmnHistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;

public class HistoryDecisionTableListener implements DmnDecisionTableEvaluationListener {

  protected DmnHistoryEventProducer eventProducer;
  protected HistoryLevel historyLevel;

  public HistoryDecisionTableListener(DmnHistoryEventProducer historyEventProducer, HistoryLevel historyLevel) {
    this.eventProducer = historyEventProducer;
    this.historyLevel = historyLevel;
  }

  public void notify(DmnDecisionTableEvaluationEvent evaluationEvent) {
   HistoryEvent historyEvent = createHistoryEvent(evaluationEvent);

    if(historyEvent != null) {
      Context.getProcessEngineConfiguration()
        .getHistoryEventHandler()
        .handleEvent(historyEvent);
    }
  }

  protected HistoryEvent createHistoryEvent(DmnDecisionTableEvaluationEvent evaluationEvent) {
    DmnDecision decisionTable = evaluationEvent.getDecisionTable();
    if(isDeployedDecisionTable(decisionTable) && historyLevel.isHistoryEventProduced(HistoryEventTypes.DMN_DECISION_EVALUATE, decisionTable)) {

      CoreExecutionContext<? extends CoreExecution> executionContext = Context.getCoreExecutionContext();
      if (executionContext != null) {
        CoreExecution coreExecution = executionContext.getExecution();

        if (coreExecution instanceof ExecutionEntity) {
          ExecutionEntity execution = (ExecutionEntity) coreExecution;
          return eventProducer.createDecisionEvaluatedEvt(execution, evaluationEvent);
        }
        else if (coreExecution instanceof CaseExecutionEntity) {
          CaseExecutionEntity caseExecution = (CaseExecutionEntity) coreExecution;
          return eventProducer.createDecisionEvaluatedEvt(caseExecution, evaluationEvent);
        }

      }

      return eventProducer.createDecisionEvaluatedEvt(evaluationEvent);

    } else {
      return null;
    }
  }

  protected boolean isDeployedDecisionTable(DmnDecision decisionTable) {
    if(decisionTable instanceof DecisionDefinition) {
      // ignore decisions that are evaluated in a script task
      return ((DecisionDefinition) decisionTable).getId() != null;
    } else {
      return false;
    }
  }

}
