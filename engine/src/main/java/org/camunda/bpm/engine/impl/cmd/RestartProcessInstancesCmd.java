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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.HistoricDetailQueryImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessInstantiationBuilderImpl;
import org.camunda.bpm.engine.impl.RestartProcessInstanceBuilderImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

/**
 *
 * @author Anna Pazola
 *
 */
public class RestartProcessInstancesCmd extends AbstractRestartProcessInstanceCmd<Void> {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public RestartProcessInstancesCmd(CommandExecutor commandExecutor,
                                    RestartProcessInstanceBuilderImpl builder) {
    super(commandExecutor, builder);
  }

  @Override
  public Void execute(final CommandContext commandContext) {
    List<AbstractProcessInstanceModificationCommand> instructions = builder.getInstructions();

    ensureNotEmpty(BadUserRequestException.class,
        "Restart instructions cannot be empty", "instructions", instructions);

    Collection<String> processInstanceIds = collectProcessInstanceIds();

    ensureNotEmpty(BadUserRequestException.class,
        "Process instance ids cannot be empty", "Process instance ids", processInstanceIds);

    ensureNotContainsNull(BadUserRequestException.class,
        "Process instance ids cannot be null", "Process instance ids", processInstanceIds);

    ProcessDefinitionEntity processDefinition =
        getProcessDefinition(commandContext, builder.getProcessDefinitionId());
    ensureNotNull("Process definition cannot be found",
        "processDefinition", processDefinition);

    checkAuthorization(commandContext, processDefinition);

    writeUserOperationLog(commandContext, processDefinition, processInstanceIds.size(), false);

    final String processDefinitionId = builder.getProcessDefinitionId();

    Runnable runnable = () -> {

      for (String processInstanceId : processInstanceIds) {
        HistoricProcessInstance historicProcessInstance =
            getHistoricProcessInstance(commandContext, processInstanceId);

        ensureNotNull(BadUserRequestException.class,
            "Historic process instance cannot be found",
            "historicProcessInstanceId", historicProcessInstance);

        ensureHistoricProcessInstanceNotActive(historicProcessInstance);
        ensureSameProcessDefinition(historicProcessInstance, processDefinitionId);

        ProcessInstantiationBuilderImpl instantiationBuilder =
            getProcessInstantiationBuilder(commandExecutor, processDefinitionId);
        applyProperties(instantiationBuilder, processDefinition, historicProcessInstance);

        ProcessInstanceModificationBuilderImpl modificationBuilder =
            instantiationBuilder.getModificationBuilder();

        modificationBuilder.setModificationOperations(instructions);

        VariableMap variables = collectVariables(commandContext, historicProcessInstance);
        instantiationBuilder.setVariables(variables);

        instantiationBuilder.execute(builder.isSkipCustomListeners(), builder.isSkipIoMappings());
      }
    };

    ProcessApplicationContextUtil.doContextSwitch(runnable, processDefinition);

    return null;
  }

  protected void checkAuthorization(CommandContext commandContext,
                                    ProcessDefinition processDefinition) {
    commandContext.getAuthorizationManager()
        .checkAuthorization(Permissions.READ_HISTORY, Resources.PROCESS_DEFINITION,
            processDefinition.getKey());
  }

  protected HistoricProcessInstance getHistoricProcessInstance(CommandContext commandContext,
                                                               String processInstanceId) {
    HistoryService historyService = commandContext.getProcessEngineConfiguration()
        .getHistoryService();
    return historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();
  }

  protected void ensureSameProcessDefinition(HistoricProcessInstance instance,
                                             String processDefinitionId) {
    if (!processDefinitionId.equals(instance.getProcessDefinitionId())) {
      throw LOG.processDefinitionOfHistoricInstanceDoesNotMatchTheGivenOne(
          instance, processDefinitionId);
    }
  }

  protected void ensureHistoricProcessInstanceNotActive(HistoricProcessInstance instance) {
    if (instance.getEndTime() == null) {
      throw LOG.historicProcessInstanceActive(instance);
    }
  }

  protected ProcessInstantiationBuilderImpl getProcessInstantiationBuilder(
      CommandExecutor commandExecutor, String processDefinitionId) {
    return (ProcessInstantiationBuilderImpl) ProcessInstantiationBuilderImpl
        .createProcessInstanceById(commandExecutor, processDefinitionId);
  }

  protected void applyProperties(ProcessInstantiationBuilderImpl instantiationBuilder,
                                 ProcessDefinition processDefinition,
                                 HistoricProcessInstance processInstance) {
    String tenantId = processInstance.getTenantId();
    if (processDefinition.getTenantId() == null && tenantId != null) {
      instantiationBuilder.tenantId(tenantId);
    }

    if (!builder.isWithoutBusinessKey()) {
      instantiationBuilder.businessKey(processInstance.getBusinessKey());
    }

  }

  protected VariableMap collectVariables(CommandContext commandContext,
                                         HistoricProcessInstance processInstance) {
    VariableMap variables = null;

    if (builder.isInitialVariables()) {
      variables = collectInitialVariables(commandContext, processInstance);
    }
    else {
      variables = collectLastVariables(commandContext, processInstance);
    }

    return variables;
  }

  protected VariableMap collectInitialVariables(CommandContext commandContext,
                                                HistoricProcessInstance processInstance) {
    HistoryService historyService = commandContext.getProcessEngineConfiguration()
        .getHistoryService();

    List<HistoricDetail> historicDetails =
        historyService.createHistoricDetailQuery()
        .variableUpdates()
        .executionId(processInstance.getId())
        .initial()
        .list();

    // legacy behavior < 7.13: the initial flag is never set for instances started
    // in these versions. We must perform the old logic of finding initial variables
    if (historicDetails.size() == 0) {
      HistoricActivityInstance startActivityInstance = resolveStartActivityInstance(processInstance);

      if (startActivityInstance != null) {
        HistoricDetailQueryImpl queryWithStartActivities = (HistoricDetailQueryImpl) historyService.createHistoricDetailQuery()
                .variableUpdates()
                .activityInstanceId(startActivityInstance.getId())
                .executionId(processInstance.getId());
        historicDetails = queryWithStartActivities
               .sequenceCounter(1)
               .list();
      }
    }

    VariableMap variables = new VariableMapImpl();
    for (HistoricDetail detail : historicDetails) {
      HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) detail;
      variables.putValueTyped(variableUpdate.getVariableName(), variableUpdate.getTypedValue());
    }

    return variables;
  }

  protected VariableMap collectLastVariables(CommandContext commandContext,
                                             HistoricProcessInstance processInstance) {
    HistoryService historyService = commandContext.getProcessEngineConfiguration()
        .getHistoryService();

    List<HistoricVariableInstance> historicVariables =
        historyService.createHistoricVariableInstanceQuery()
            .executionIdIn(processInstance.getId())
            .list();

    VariableMap variables = new VariableMapImpl();
    for (HistoricVariableInstance variable : historicVariables) {
      variables.putValueTyped(variable.getName(), variable.getTypedValue());
    }

    return variables;
  }

  protected HistoricActivityInstance resolveStartActivityInstance(
      HistoricProcessInstance processInstance) {
    HistoryService historyService = Context.getProcessEngineConfiguration().getHistoryService();

    String processInstanceId = processInstance.getId();
    String startActivityId = processInstance.getStartActivityId();

    ensureNotNull("startActivityId", startActivityId);

    List<HistoricActivityInstance> historicActivityInstances = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId)
        .activityId(startActivityId)
        .orderPartiallyByOccurrence()
        .asc()
        .list();

    ensureNotEmpty("historicActivityInstances", historicActivityInstances);

    HistoricActivityInstance startActivityInstance = historicActivityInstances.get(0);
    return startActivityInstance;
  }

}
