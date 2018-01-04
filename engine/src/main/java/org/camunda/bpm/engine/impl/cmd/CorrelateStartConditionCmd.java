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

package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ConditionCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.bpmn.parser.ConditionalEventDefinition;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;

public class CorrelateStartConditionCmd implements Command<List<ProcessInstance>> {

  protected VariableMap processInstanceVariables;
  protected String tenantId;
  protected String processDefinitionId;
  protected String businessKey;

  public CorrelateStartConditionCmd(VariableMap processInstanceVariables, String tenantId) {
    this.processInstanceVariables = processInstanceVariables;
    this.tenantId = tenantId;
  }

  public CorrelateStartConditionCmd(ConditionCorrelationBuilderImpl builder) {
    this.processInstanceVariables = builder.getProcessInstanceVariables();
    if (builder.isTenantIdSet()) {
      this.tenantId = builder.getTenantId();
    }
    if (builder.getProcessDefinitionId() != null) {
      processDefinitionId = builder.getProcessDefinitionId();
    }
    if (builder.getBusinessKey() != null) {
      businessKey = builder.getBusinessKey();
    }
  }

  @Override
  public List<ProcessInstance> execute(CommandContext commandContext) {
    ensureNotNull(BadUserRequestException.class, "Variables are mandatory to start process instance by condition", "variables", processInstanceVariables);

    EventSubscriptionManager eventSubscriptionManager = commandContext.getEventSubscriptionManager();
    List<EventSubscriptionEntity> subscriptions = eventSubscriptionManager.findConditionalStartEventSubscriptionByTenantId(tenantId);
    if (subscriptions == null || subscriptions.isEmpty()) {
      throw new ProcessEngineException("No subscriptions were found during correlation of the conditional start events.");
    }

    List<ConditionalResult> results = new ArrayList<CorrelateStartConditionCmd.ConditionalResult>();
    for (EventSubscriptionEntity subscription : subscriptions) {

      ProcessDefinitionEntity processDefinition = subscription.getProcessDefinition();
      if ((processDefinitionId != null && !processDefinitionId.equals(processDefinition.getId()))
          || (businessKey != null && !businessKey.equals(processDefinition.getKey()))) {
        continue;
      }
      if (!processDefinition.isSuspended()) {

        ExecutionEntity temporaryExecution = new ExecutionEntity();
        temporaryExecution.initializeVariableStore(processInstanceVariables);
        temporaryExecution.setProcessDefinition(processDefinition);

        ActivityImpl activity = subscription.getActivity();
        ConditionalEventDefinition conditionalEventDefinition = activity.getProperties().get(BpmnProperties.CONDITIONAL_EVENT_DEFINITION);
        try {
          if (conditionalEventDefinition.evaluate(temporaryExecution)) {
            results.add(new ConditionalResult(processDefinition, activity));
          }
        } catch (ProcessEngineException e) {
          if (!e.getMessage().contains("Unknown property used in expression:")) {
            throw e;
          }
        }

      }
    }

    if (results.isEmpty()) {
      throw new ProcessEngineException("No process instances were started during correlation of the conditional start events.");
    }

    for (ConditionalResult conditionalResult : results) {
      checkAuthorization(commandContext, conditionalResult);
    }

    List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();
    for (ConditionalResult conditionalResult : results) {
      processInstances.add(instantiateProcess(commandContext, conditionalResult));
    }

    return processInstances;
  }

  protected void checkAuthorization(CommandContext commandContext, ConditionalResult result) {
    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      ProcessDefinitionEntity definition = result.processDefinition;
      checker.checkCreateProcessInstance(definition);
    }
  }

  protected ProcessInstance instantiateProcess(CommandContext commandContext, ConditionalResult result) {
    ProcessDefinitionEntity processDefinitionEntity = result.processDefinition;

    ActivityImpl startEvent = processDefinitionEntity.findActivity(result.activity.getActivityId());
    ExecutionEntity processInstance = processDefinitionEntity.createProcessInstance(null, startEvent);
    processInstance.start(processInstanceVariables);

    return processInstance;
  }

  class ConditionalResult {

    protected ProcessDefinitionEntity processDefinition;
    protected ActivityImpl activity;

    public ConditionalResult(ProcessDefinitionEntity processDefinition, ActivityImpl activity) {
      this.processDefinition = processDefinition;
      this.activity = activity;
    }

  }

}
