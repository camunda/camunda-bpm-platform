package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.ProcessInstantiationBuilderImpl;
import org.camunda.bpm.engine.impl.RestartProcessInstanceBuilderImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

/**
 * 
 * @author Anna Pazola
 *
 */
public class RestartProcessInstancesCmd extends AbstractRestartProcessInstanceCmd<Void> {

  protected boolean writeUserOperationLog;
  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public RestartProcessInstancesCmd(CommandExecutor commandExecutor, RestartProcessInstanceBuilderImpl builder, boolean writeUserOperationLog) {
    super(commandExecutor, builder);
    this.writeUserOperationLog = writeUserOperationLog;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    HistoryService historyService = commandContext.getProcessEngineConfiguration().getHistoryService();
    Collection<String> processInstanceIds = collectProcessInstanceIds();
    List<AbstractProcessInstanceModificationCommand> instructions = builder.getInstructions();
    ensureNotEmpty(BadUserRequestException.class, "instructions", instructions);

    ProcessDefinitionEntity processDefinition;
    try {
      processDefinition = getProcessDefinition(commandContext, builder.getProcessDefinitionId());
    } catch (NullValueException e) {
      throw new BadUserRequestException(e.getMessage());
    }
    ensureNotNull(BadUserRequestException.class, "Process definition id cannot be null", processDefinition);
    if (writeUserOperationLog) {
      writeUserOperationLog(commandContext, processDefinition, processInstanceIds.size(), false);
    }

    for (String processInstanceId : processInstanceIds) {
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
      ensureNotNull(BadUserRequestException.class, "the historic process instance cannot be found", "historicProcessInstanceId", historicProcessInstance);
      String processDefinitionId = builder.getProcessDefinitionId();
      ensureSameProcessDefinition(historicProcessInstance, processDefinitionId);

      ProcessInstantiationBuilderImpl instantiationBuilder = (ProcessInstantiationBuilderImpl) ProcessInstantiationBuilderImpl
          .createProcessInstanceById(commandExecutor, processDefinitionId);

      ProcessInstanceModificationBuilderImpl modificationBuilder = new ProcessInstanceModificationBuilderImpl();
      modificationBuilder.setModificationOperations(instructions);
      instantiationBuilder.setModificationBuilder(modificationBuilder);
      instantiationBuilder.caseInstanceId(historicProcessInstance.getCaseInstanceId());
      instantiationBuilder.businessKey(historicProcessInstance.getBusinessKey());

      List<HistoricVariableInstance> historicVariables = historyService.createHistoricVariableInstanceQuery().executionIdIn(processInstanceId).list();
      for (HistoricVariableInstance historicVariable : historicVariables) {
        instantiationBuilder.setVariable(historicVariable.getName(), historicVariable.getValue());
      }

      instantiationBuilder.execute();
    }
    return null;
  }

  protected void ensureSameProcessDefinition(HistoricProcessInstance instance, String processDefinitionId) {
    if (!processDefinitionId.equals(instance.getProcessDefinitionId())) {
      throw LOG.processDefinitionOfHistoricInstanceDoesNotMatchTheGivenOne(instance, processDefinitionId);
    }
  }
}
