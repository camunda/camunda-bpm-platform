package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * 
 * @author Anna Pazola
 *
 */
public class CreateIncidentCmd implements Command<Incident> {

  protected String incidentType;
  protected String executionId;
  protected String configuration;
  protected String message;

  public CreateIncidentCmd(String incidentType, String executionId, String configuration, String message) {
    this.incidentType = incidentType;
    this.executionId = executionId;
    this.configuration = configuration;
    this.message = message;
  }

  @Override
  public Incident execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Execution id cannot be null", "executionId", executionId);
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "incidentType", incidentType);

    ExecutionEntity execution = commandContext.getExecutionManager().findExecutionById(executionId);
    EnsureUtil.ensureNotNull(BadUserRequestException.class,
        "Cannot find an execution with executionId '" + executionId + "'", "execution", execution);
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Execution must be related to an activity", "activity",
        execution.getActivity());

    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstance(execution);
    }

    return execution.createIncident(incidentType, configuration, message);
  }
}
