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
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLiteralExpressionEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLogicEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedInput;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.history.HistoricDecisionInputInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderHistoricDecisionInstanceContext;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionEvaluationEvent;
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
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 * @author Ingo Richtsmeier
 */
public class DefaultDmnHistoryEventProducer implements DmnHistoryEventProducer {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  public HistoryEvent createDecisionEvaluatedEvt(final DelegateExecution execution, final DmnDecisionEvaluationEvent evaluationEvent) {
    return createHistoryEvent(evaluationEvent, new HistoricDecisionInstanceSupplier() {

      public HistoricDecisionInstanceEntity createHistoricDecisionInstance(DmnDecisionLogicEvaluationEvent evaluationEvent) {
        return createDecisionEvaluatedEvt(evaluationEvent, (ExecutionEntity) execution);
      }
    });
  }

  public HistoryEvent createDecisionEvaluatedEvt(final DelegateCaseExecution execution, final DmnDecisionEvaluationEvent evaluationEvent) {
    return createHistoryEvent(evaluationEvent, new HistoricDecisionInstanceSupplier() {

      public HistoricDecisionInstanceEntity createHistoricDecisionInstance(DmnDecisionLogicEvaluationEvent evaluationEvent) {
        return createDecisionEvaluatedEvt(evaluationEvent, (CaseExecutionEntity) execution);
      }
    });
  }

  public HistoryEvent createDecisionEvaluatedEvt(final DmnDecisionEvaluationEvent evaluationEvent) {
    return createHistoryEvent(evaluationEvent, new HistoricDecisionInstanceSupplier() {

      public HistoricDecisionInstanceEntity createHistoricDecisionInstance(DmnDecisionLogicEvaluationEvent evaluationEvent) {
        return createDecisionEvaluatedEvt(evaluationEvent);
      }
    });
  }

  protected interface HistoricDecisionInstanceSupplier {
    HistoricDecisionInstanceEntity createHistoricDecisionInstance(DmnDecisionLogicEvaluationEvent evaluationEvent);
  }

  protected HistoryEvent createHistoryEvent(DmnDecisionEvaluationEvent evaluationEvent, HistoricDecisionInstanceSupplier supplier) {
    HistoricDecisionEvaluationEvent event = newDecisionEvaluationEvent(evaluationEvent);

    HistoricDecisionInstanceEntity rootDecisionEvent = supplier.createHistoricDecisionInstance(evaluationEvent.getDecisionResult());
    event.setRootHistoricDecisionInstance(rootDecisionEvent);

    List<HistoricDecisionInstanceEntity> requiredDecisionEvents = new ArrayList<HistoricDecisionInstanceEntity>();
    for (DmnDecisionLogicEvaluationEvent requiredDecisionResult : evaluationEvent.getRequiredDecisionResults()) {
      HistoricDecisionInstanceEntity requiredDecisionEvent = supplier.createHistoricDecisionInstance(requiredDecisionResult);
      requiredDecisionEvents.add(requiredDecisionEvent);
    }
    event.setRequiredHistoricDecisionInstances(requiredDecisionEvents);

    return event;
  }

  protected HistoricDecisionInstanceEntity createDecisionEvaluatedEvt(DmnDecisionLogicEvaluationEvent evaluationEvent, ExecutionEntity execution) {
    // create event instance
    HistoricDecisionInstanceEntity event = newDecisionInstanceEventEntity(execution, evaluationEvent);
    // initialize event
    initDecisionInstanceEvent(event, evaluationEvent, HistoryEventTypes.DMN_DECISION_EVALUATE);
    setReferenceToProcessInstance(event, execution);
    // set current time as evaluation time
    event.setEvaluationTime(ClockUtil.getCurrentTime());

    DecisionDefinition decisionDefinition = (DecisionDefinition) evaluationEvent.getDecision();
    String tenantId = execution.getTenantId();
    if (tenantId == null) {
      tenantId = provideTenantId(decisionDefinition, event);
    }
    event.setTenantId(tenantId);
    return event;
  }

  protected HistoricDecisionInstanceEntity createDecisionEvaluatedEvt(DmnDecisionLogicEvaluationEvent evaluationEvent, CaseExecutionEntity execution) {
    // create event instance
    HistoricDecisionInstanceEntity event = newDecisionInstanceEventEntity(execution, evaluationEvent);
    // initialize event
    initDecisionInstanceEvent(event, evaluationEvent, HistoryEventTypes.DMN_DECISION_EVALUATE);
    setReferenceToCaseInstance(event, execution);
    // set current time as evaluation time
    event.setEvaluationTime(ClockUtil.getCurrentTime());

    DecisionDefinition decisionDefinition = (DecisionDefinition) evaluationEvent.getDecision();
    String tenantId = execution.getTenantId();
    if (tenantId == null) {
      tenantId = provideTenantId(decisionDefinition, event);
    }
    event.setTenantId(tenantId);
    return event;
  }

  protected HistoricDecisionInstanceEntity createDecisionEvaluatedEvt(DmnDecisionLogicEvaluationEvent evaluationEvent) {
    // create event instance
    HistoricDecisionInstanceEntity event = newDecisionInstanceEventEntity(evaluationEvent);
    // initialize event
    initDecisionInstanceEvent(event, evaluationEvent, HistoryEventTypes.DMN_DECISION_EVALUATE);
    // set current time as evaluation time
    event.setEvaluationTime(ClockUtil.getCurrentTime());
    // set the user id if there is an authenticated user and no process instance
    setUserId(event);

    DecisionDefinition decisionDefinition = (DecisionDefinition) evaluationEvent.getDecision();
    String tenantId = decisionDefinition.getTenantId();
    if (tenantId == null) {
      tenantId = provideTenantId(decisionDefinition, event);
    }
    event.setTenantId(tenantId);
    return event;
  }

  protected HistoricDecisionEvaluationEvent newDecisionEvaluationEvent(DmnDecisionEvaluationEvent evaluationEvent) {
    return new HistoricDecisionEvaluationEvent();
  }

  protected HistoricDecisionInstanceEntity newDecisionInstanceEventEntity(ExecutionEntity executionEntity, DmnDecisionLogicEvaluationEvent evaluationEvent) {
    return new HistoricDecisionInstanceEntity();
  }

  protected HistoricDecisionInstanceEntity newDecisionInstanceEventEntity(CaseExecutionEntity executionEntity, DmnDecisionLogicEvaluationEvent evaluationEvent) {
    return new HistoricDecisionInstanceEntity();
  }

  protected HistoricDecisionInstanceEntity newDecisionInstanceEventEntity(DmnDecisionLogicEvaluationEvent evaluationEvent) {
    return new HistoricDecisionInstanceEntity();
  }

  protected void initDecisionInstanceEvent(HistoricDecisionInstanceEntity event, DmnDecisionLogicEvaluationEvent evaluationEvent, HistoryEventTypes eventType) {
    event.setEventType(eventType.getEventName());

    DecisionDefinition decision = (DecisionDefinition) evaluationEvent.getDecision();
    event.setDecisionDefinitionId(decision.getId());
    event.setDecisionDefinitionKey(decision.getKey());
    event.setDecisionDefinitionName(decision.getName());

    if (decision.getDecisionRequirementsDefinitionId() != null) {
      event.setDecisionRequirementsDefinitionId(decision.getDecisionRequirementsDefinitionId());
      event.setDecisionRequirementsDefinitionKey(decision.getDecisionRequirementsDefinitionKey());
    }

    if (evaluationEvent instanceof DmnDecisionTableEvaluationEvent) {
      initDecisionInstanceEventForDecisionTable(event, (DmnDecisionTableEvaluationEvent) evaluationEvent);

    } else if (evaluationEvent instanceof DmnDecisionLiteralExpressionEvaluationEvent) {
      initDecisionInstanceEventForDecisionLiteralExpression(event, (DmnDecisionLiteralExpressionEvaluationEvent) evaluationEvent);

    } else {
      event.setInputs(Collections.<HistoricDecisionInputInstance> emptyList());
      event.setOutputs(Collections.<HistoricDecisionOutputInstance> emptyList());
    }
  }

  protected void initDecisionInstanceEventForDecisionTable(HistoricDecisionInstanceEntity event, DmnDecisionTableEvaluationEvent evaluationEvent) {
    if (evaluationEvent.getCollectResultValue() != null) {
      Double collectResultValue = getCollectResultValue(evaluationEvent.getCollectResultValue());
      event.setCollectResultValue(collectResultValue);
    }

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = createHistoricDecisionInputInstances(evaluationEvent);
    event.setInputs(historicDecisionInputInstances);

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = createHistoricDecisionOutputInstances(evaluationEvent);
    event.setOutputs(historicDecisionOutputInstances);
  }

  protected Double getCollectResultValue(TypedValue collectResultValue) {
    // the built-in collect aggregators return only numbers

    if(collectResultValue instanceof IntegerValue) {
      return ((IntegerValue) collectResultValue).getValue().doubleValue();

    } else if(collectResultValue instanceof LongValue) {
      return ((LongValue) collectResultValue).getValue().doubleValue();

    } else if(collectResultValue instanceof DoubleValue) {
      return ((DoubleValue) collectResultValue).getValue();

    } else {
      throw LOG.collectResultValueOfUnsupportedTypeException(collectResultValue);
    }
  }

  protected List<HistoricDecisionInputInstance> createHistoricDecisionInputInstances(DmnDecisionTableEvaluationEvent evaluationEvent) {
    List<HistoricDecisionInputInstance> inputInstances = new ArrayList<HistoricDecisionInputInstance>();

    for(DmnEvaluatedInput inputClause : evaluationEvent.getInputs()) {

      HistoricDecisionInputInstanceEntity inputInstance = new HistoricDecisionInputInstanceEntity();
      inputInstance.setClauseId(inputClause.getId());
      inputInstance.setClauseName(inputClause.getName());

      TypedValue typedValue = Variables.untypedValue(inputClause.getValue());
      inputInstance.setValue(typedValue);

      inputInstances.add(inputInstance);
    }

    return inputInstances;
  }

  protected List<HistoricDecisionOutputInstance> createHistoricDecisionOutputInstances(DmnDecisionTableEvaluationEvent evaluationEvent) {
    List<HistoricDecisionOutputInstance> outputInstances = new ArrayList<HistoricDecisionOutputInstance>();

    List<DmnEvaluatedDecisionRule> matchingRules = evaluationEvent.getMatchingRules();
    for(int index = 0; index < matchingRules.size(); index++) {
      DmnEvaluatedDecisionRule rule = matchingRules.get(index);

      String ruleId = rule.getId();
      Integer ruleOrder = index + 1;

      for(DmnEvaluatedOutput outputClause : rule.getOutputEntries().values()) {

        HistoricDecisionOutputInstanceEntity outputInstance = new HistoricDecisionOutputInstanceEntity();
        outputInstance.setClauseId(outputClause.getId());
        outputInstance.setClauseName(outputClause.getName());

        outputInstance.setRuleId(ruleId);
        outputInstance.setRuleOrder(ruleOrder);

        outputInstance.setVariableName(outputClause.getOutputName());
        outputInstance.setValue(outputClause.getValue());

        outputInstances.add(outputInstance);
      }
    }

    return outputInstances;
  }

  protected void initDecisionInstanceEventForDecisionLiteralExpression(HistoricDecisionInstanceEntity event, DmnDecisionLiteralExpressionEvaluationEvent evaluationEvent) {
    // no inputs for expression
    event.setInputs(Collections.<HistoricDecisionInputInstance> emptyList());

    HistoricDecisionOutputInstanceEntity outputInstance = new HistoricDecisionOutputInstanceEntity();
    outputInstance.setVariableName(evaluationEvent.getOutputName());
    outputInstance.setValue(evaluationEvent.getOutputValue());

    event.setOutputs(Collections.<HistoricDecisionOutputInstance> singletonList(outputInstance));
  }

  protected void setReferenceToProcessInstance(HistoricDecisionInstanceEntity event, ExecutionEntity execution) {
    event.setProcessDefinitionKey(getProcessDefinitionKey(execution));
    event.setProcessDefinitionId(execution.getProcessDefinitionId());

    event.setProcessInstanceId(execution.getProcessInstanceId());
    event.setExecutionId(execution.getId());

    event.setActivityId(execution.getActivityId());
    event.setActivityInstanceId(execution.getActivityInstanceId());
  }

  protected String getProcessDefinitionKey(ExecutionEntity execution) {
    ProcessDefinitionEntity definition = execution.getProcessDefinition();
    if (definition != null) {
      return definition.getKey();
    } else {
      return null;
    }
  }

  protected void setReferenceToCaseInstance(HistoricDecisionInstanceEntity event, CaseExecutionEntity execution) {
    event.setCaseDefinitionKey(getCaseDefinitionKey(execution));
    event.setCaseDefinitionId(execution.getCaseDefinitionId());

    event.setCaseInstanceId(execution.getCaseInstanceId());
    event.setExecutionId(execution.getId());

    event.setActivityId(execution.getActivityId());
    event.setActivityInstanceId(execution.getId());
  }

  protected String getCaseDefinitionKey(CaseExecutionEntity execution) {
    CaseDefinitionEntity definition = (CaseDefinitionEntity) execution.getCaseDefinition();
    if (definition != null) {
      return definition.getKey();
    } else {
      return null;
    }
  }

  protected void setUserId(HistoricDecisionInstanceEntity event) {
    event.setUserId(Context.getCommandContext().getAuthenticatedUserId());
  }

  protected String provideTenantId(DecisionDefinition decisionDefinition, HistoricDecisionInstanceEntity event) {
    TenantIdProvider tenantIdProvider = Context.getProcessEngineConfiguration().getTenantIdProvider();
    String tenantId = null;

    if(tenantIdProvider != null) {
      TenantIdProviderHistoricDecisionInstanceContext ctx = null;

      if(event.getExecutionId() != null) {
        ctx = new TenantIdProviderHistoricDecisionInstanceContext(decisionDefinition, getExecution(event));
      }
      else if(event.getCaseExecutionId() != null) {
        ctx = new TenantIdProviderHistoricDecisionInstanceContext(decisionDefinition, getCaseExecution(event));
      }
      else {
        ctx = new TenantIdProviderHistoricDecisionInstanceContext(decisionDefinition);
      }

      tenantId = tenantIdProvider.provideTenantIdForHistoricDecisionInstance(ctx);
    }

    return tenantId;
  }

  protected DelegateExecution getExecution(HistoricDecisionInstanceEntity event) {
    return Context.getCommandContext().getExecutionManager().findExecutionById(event.getExecutionId());
  }

  protected DelegateCaseExecution getCaseExecution(HistoricDecisionInstanceEntity event) {
      return Context.getCommandContext().getCaseExecutionManager().findCaseExecutionById(event.getCaseExecutionId());
  }

}
