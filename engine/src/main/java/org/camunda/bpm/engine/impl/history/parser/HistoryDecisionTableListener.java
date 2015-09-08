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

import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableListener;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.DmnHistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;

public class HistoryDecisionTableListener implements DmnDecisionTableListener {

  protected DmnHistoryEventProducer eventProducer;
  protected HistoryLevel historyLevel;

  public HistoryDecisionTableListener(DmnHistoryEventProducer historyEventProducer, HistoryLevel historyLevel) {
    this.eventProducer = historyEventProducer;
    this.historyLevel = historyLevel;
  }

  public void notify(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    ExecutionEntity execution = Context.getBpmnExecutionContext().getExecution();

    HistoryEvent historyEvent = createHistoryEvent(execution, decisionTable, decisionTableResult);

    if(historyEvent != null) {
      Context.getProcessEngineConfiguration()
        .getHistoryEventHandler()
        .handleEvent(historyEvent);
    }

  }

  public HistoryEvent createHistoryEvent(DelegateExecution execution, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    if(historyLevel.isHistoryEventProduced(HistoryEventTypes.DMN_DECISION_EVALUATE, null) && isDeployedDecisionTable(decisionTable)) {
      return eventProducer.createDecisionEvaluatedEvt(execution, decisionTable, decisionTableResult);
    } else {
      return null;
    }
  }

  protected boolean isDeployedDecisionTable(DmnDecisionTable decisionTable) {
    if(decisionTable instanceof DecisionDefinition) {
      // ignore decisions that are evaluated in a script task
      return ((DecisionDefinition) decisionTable).getId() != null;
    } else {
      return false;
    }
  }

}
