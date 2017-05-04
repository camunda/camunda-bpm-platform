package org.camunda.bpm.engine.impl.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.RestartProcessInstanceBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;

public abstract class AbstractRestartProcessInstanceCmd<T> implements Command<T>{

  protected CommandExecutor commandExecutor;
  protected RestartProcessInstanceBuilderImpl builder;

  public AbstractRestartProcessInstanceCmd(CommandExecutor commandExecutor, RestartProcessInstanceBuilderImpl builder) {
    this.commandExecutor = commandExecutor;
    this.builder = builder;
  }

  protected Collection<String> collectProcessInstanceIds() {

    Set<String> collectedProcessInstanceIds = new HashSet<String>();

    List<String> processInstanceIds = builder.getProcessInstanceIds();
    if (processInstanceIds != null) {
      collectedProcessInstanceIds.addAll(processInstanceIds);
    }

    final HistoricProcessInstanceQueryImpl historicProcessInstanceQuery = (HistoricProcessInstanceQueryImpl) builder.getHistoricProcessInstanceQuery();
    if (historicProcessInstanceQuery != null) {
      collectedProcessInstanceIds.addAll(historicProcessInstanceQuery.listIds());
    }

    EnsureUtil.ensureNotEmpty(BadUserRequestException.class, "processInstanceIds", collectedProcessInstanceIds);
    return collectedProcessInstanceIds;
  }
  
  protected void writeUserOperationLog(CommandContext commandContext,
      ProcessDefinition processDefinition,
      int numInstances,
      boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances",
        null,
        numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));

    commandContext.getOperationLogManager()
      .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_RESTART_PROCESS_INSTANCE,
          null,
          processDefinition.getId(),
          processDefinition.getKey(),
          propertyChanges);
  }

  protected ProcessDefinitionEntity getProcessDefinition(CommandContext commandContext, String processDefinitionId) {

    return commandContext
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedProcessDefinitionById(processDefinitionId);
  }
}
