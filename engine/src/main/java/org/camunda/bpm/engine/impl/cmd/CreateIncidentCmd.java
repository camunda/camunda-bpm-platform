package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
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
  protected String activityId;
  protected String configuration;
  protected String message;

  public CreateIncidentCmd(String incidentType, String executionId, String activityId, String configuration, String message) {
    this.incidentType = incidentType;
    this.executionId = executionId;
    this.activityId = activityId;
    this.configuration = configuration;
    this.message = message;
  }

  @Override
  public Incident execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Execution id cannot be null", "executionId", executionId);
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "incidentType", incidentType);
    ExecutionEntity execution = (ExecutionEntity) new ExecutionQueryImpl().executionId(executionId).activityId(activityId).singleResult();
    EnsureUtil.ensureNotNull(BadUserRequestException.class,
        "Cannot find an execution with executionId '" + executionId + "' and activityId '" + activityId + "'", "execution", execution);
    return execution.createIncident(incidentType, configuration, message);
  }
}
