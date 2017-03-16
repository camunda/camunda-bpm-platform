package org.camunda.bpm.engine.impl.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ModificationBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.repository.ProcessDefinition;

public abstract class AbstractModificationCmd<T> implements Command<T> {

  protected ModificationBuilderImpl builder;

  public AbstractModificationCmd(ModificationBuilderImpl modificationBuilderImpl) {
    this.builder = modificationBuilderImpl;
  }

  protected Collection<String> collectProcessInstanceIds(CommandContext commandContext) {

    Set<String> collectedProcessInstanceIds = new HashSet<String>();

    List<String> processInstanceIds = builder.getProcessInstanceIds();
    if (processInstanceIds != null) {
      collectedProcessInstanceIds.addAll(processInstanceIds);
    }

    final ProcessInstanceQueryImpl processInstanceQuery = (ProcessInstanceQueryImpl) builder.getProcessInstanceQuery();
    if (processInstanceQuery != null) {
      collectedProcessInstanceIds.addAll(processInstanceQuery.listIds());
    }

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
      .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE,
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
