/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.history.producer;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.dmn.engine.DmnDecisionTableValue;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 */
public class DefaultDmnHistoryEventProducer implements DmnHistoryEventProducer {

  @Override
  public HistoryEvent createDecisionEvaluatedEvt(DelegateExecution execution, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    final ExecutionEntity executionEntity = (ExecutionEntity) execution;

    // create event instance
    HistoricDecisionInstanceEntity event = newDecisionInstanceEventEntity(executionEntity, decisionTable, decisionTableResult);
    // initialize event
    initDecisionInstanceEvent(event, executionEntity, decisionTable, decisionTableResult, HistoryEventTypes.DMN_DECISION_EVALUATE);
    // set current time as evaluation time
    event.setEvaluationTime(ClockUtil.getCurrentTime());

    return event;
  }

  protected HistoricDecisionInstanceEntity newDecisionInstanceEventEntity(ExecutionEntity executionEntity, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    return new HistoricDecisionInstanceEntity();
  }

  protected void initDecisionInstanceEvent(HistoricDecisionInstanceEntity event, ExecutionEntity execution, DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult, HistoryEventTypes eventType) {
    event.setEventType(eventType.getEventName());

    event.setDecisionDefinitionId(((DecisionDefinition) decisionTable).getId());
    event.setDecisionDefinitionKey(decisionTable.getKey());
    event.setDecisionDefinitionName(decisionTable.getName());

    event.setProcessDefinitionKey(getProcessDefinitionKey(execution));
    event.setProcessDefinitionId(execution.getProcessDefinitionId());

    event.setProcessInstanceId(execution.getProcessInstanceId());

    event.setActivityId(execution.getActivityId());
    event.setActivityInstanceId(execution.getActivityInstanceId());

    if(decisionTableResult.getCollectResultValue() != null) {
      double collectResultValue = decisionTableResult.getCollectResultValue().doubleValue();
      event.setCollectResultValue(collectResultValue);
    }

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = createHistoricDecisionInputInstances(decisionTableResult);
    event.setInputs(historicDecisionInputInstances);

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = createHistoricDecisionOutputInstances(decisionTableResult);
    event.setOutputs(historicDecisionOutputInstances);
  }

  protected String getProcessDefinitionKey(ExecutionEntity execution) {
    ProcessDefinitionEntity definition = (ProcessDefinitionEntity) execution.getProcessDefinition();
    if (definition != null) {
      return definition.getKey();
    } else {
      return null;
    }
  }

  protected List<HistoricDecisionInputInstance> createHistoricDecisionInputInstances(DmnDecisionTableResult decisionTableResult) {
    List<HistoricDecisionInputInstance> inputInstances = new ArrayList<HistoricDecisionInputInstance>();

    for(DmnDecisionTableValue inputClause : decisionTableResult.getInputs().values()) {

      HistoricDecisionInputInstanceEntity inputInstance = new HistoricDecisionInputInstanceEntity();
      inputInstance.setClauseId(inputClause.getKey());
      inputInstance.setClauseName(inputClause.getName());

      TypedValue typedValue = Variables.untypedValue(inputClause.getValue());
      inputInstance.setValue(typedValue);

      inputInstances.add(inputInstance);
    }

    return inputInstances;
  }

  protected List<HistoricDecisionOutputInstance> createHistoricDecisionOutputInstances(DmnDecisionTableResult decisionTableResult) {
    List<HistoricDecisionOutputInstance> outputInstances = new ArrayList<HistoricDecisionOutputInstance>();

    List<DmnDecisionTableRule> matchingRules = decisionTableResult.getMatchingRules();
    for(int index = 0; index < matchingRules.size(); index++) {
      DmnDecisionTableRule rule = matchingRules.get(index);

      String ruleId = rule.getKey();
      Integer ruleOrder = index + 1;

      for(DmnDecisionTableValue outputClause : rule.getOutputs().values()) {

        HistoricDecisionOutputInstanceEntity outputInstance = new HistoricDecisionOutputInstanceEntity();
        outputInstance.setClauseId(outputClause.getKey());
        outputInstance.setClauseName(outputClause.getName());

        outputInstance.setRuleId(ruleId);
        outputInstance.setRuleOrder(ruleOrder);

        outputInstance.setVariableName(outputClause.getOutputName());

        TypedValue typedValue = Variables.untypedValue(outputClause.getValue());
        outputInstance.setValue(typedValue);

        outputInstances.add(outputInstance);
      }
    }

    return outputInstances;
  }

}
