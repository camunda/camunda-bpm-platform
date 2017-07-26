package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.NotFoundException;
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
public class ResolveIncidentCmd implements Command<Void> {

  protected String incidentId;

  public ResolveIncidentCmd(String incidentId) {
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "", "incidentId", incidentId);
    this.incidentId = incidentId;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    final Incident incident = commandContext.getIncidentManager().findIncidentById(incidentId);

    EnsureUtil.ensureNotNull(NotFoundException.class, "Cannot find an incident with id '" + incidentId + "'",
        "incident", incident);

    if (incident.getIncidentType().equals("failedJob") || incident.getIncidentType().equals("failedExternalTask")) {
      throw new BadUserRequestException("Cannot resolve an incident of type " + incident.getIncidentType());
    }

    EnsureUtil.ensureNotNull(BadUserRequestException.class, "", "executionId", incident.getExecutionId());
    ExecutionEntity execution = commandContext.getExecutionManager().findExecutionById(incident.getExecutionId());

    EnsureUtil.ensureNotNull(BadUserRequestException.class,
        "Cannot find an execution for an incident with id '" + incidentId + "'", "execution", execution);

    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstance(execution);
    }

    execution.resolveIncident(incidentId);
    return null;
  }
}
