package org.camunda.bpm.engine.impl.jobexecutor;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.repository.CaseDefinition;

/**
 * @author Roman Smirnov
 * @author Subhro
 */
public class TimerEventListenerJobHandler extends TimerEventJobHandler {

  public static final String TYPE = "timer-event-listener";

  public String getType() {
    return TYPE;
  }

  public void execute(String configuration, CoreExecution context, CommandContext commandContext, String tenantId) {
    DeploymentCache deploymentCache = Context.getProcessEngineConfiguration().getDeploymentCache();
    String definitionKey = getKey(configuration);
    CaseDefinition definition = deploymentCache.findDeployedLatestCaseDefinitionByKeyAndTenantId(definitionKey, tenantId);
    //TODO the occur event here??
  }

}
