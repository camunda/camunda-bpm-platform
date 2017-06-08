package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.IncidentQueryImpl;
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
    this.incidentId = incidentId;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    Incident incident = new IncidentQueryImpl().incidentId(incidentId).singleResult();
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Cannot find an incident with id '" + incidentId + "'", "incident", incident);
    ExecutionEntity execution = (ExecutionEntity) new ExecutionQueryImpl().executionId(incident.getExecutionId()).singleResult();
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Cannot find an execution for an incident with id '" + incidentId + "'", "execution", execution);
    execution.resolveIncident(incidentId);
    return null;
  }
}
