package org.camunda.bpm.engine.impl.cmd;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.ExecutionQueryImpl;
import org.camunda.bpm.engine.impl.IncidentQueryImpl;
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
    this.incidentId = incidentId;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    final Incident incident = commandContext.runWithoutAuthorization(new Callable<Incident>() {

      @Override
      public Incident call() throws Exception {
        return new IncidentQueryImpl().incidentId(incidentId).singleResult();
      }
    });

    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Cannot find an incident with id '" + incidentId + "'", "incident", incident);
    EnsureUtil.ensureNull(BadUserRequestException.class, "Cannot resolve an incident that belongs to a job definition", "jobDefinitionId", incident.getJobDefinitionId());

    ExecutionEntity execution = commandContext.runWithoutAuthorization(new Callable<ExecutionEntity>() {

      @Override
      public ExecutionEntity call() throws Exception {
         return (ExecutionEntity) new ExecutionQueryImpl().executionId(incident.getExecutionId()).singleResult();
      }
    });

    EnsureUtil.ensureNotNull(BadUserRequestException.class, "Cannot find an execution for an incident with id '" + incidentId + "'", "execution", execution);

    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateProcessInstance(execution);
    }

    execution.resolveIncident(incidentId);
    return null;
  }
}
