package org.camunda.bpm.engine.rest.util;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;

public class ApplicationContextPathUtil {

  public static String getApplicationPathByProcessDefinitionId(ProcessEngine engine, String processDefinitionId) {
    ProcessDefinition processDefinition = engine.getRepositoryService().getProcessDefinition(processDefinitionId);

    if (processDefinition == null) {
      return null;
    }

    return getApplicationPathForDeployment(engine, processDefinition.getDeploymentId());
  }

  public static String getApplicationPathByCaseDefinitionId(ProcessEngine engine, String caseDefinitionId) {
    CaseDefinition caseDefinition = engine.getRepositoryService().getCaseDefinition(caseDefinitionId);

    if (caseDefinition == null) {
      return null;
    }

    return getApplicationPathForDeployment(engine, caseDefinition.getDeploymentId());
  }

  public static String getApplicationPathForDeployment(ProcessEngine engine, String deploymentId) {

    // get the name of the process application that made the deployment
    String processApplicationName = null;
    IdentityService identityService = engine.getIdentityService();
    Authentication currentAuthentication = identityService.getCurrentAuthentication();
    try {
      identityService.clearAuthentication();
      processApplicationName = engine.getManagementService().getProcessApplicationForDeployment(deploymentId);
    } finally {
      identityService.setAuthentication(currentAuthentication);
    }

    if (processApplicationName == null) {
      // no a process application deployment
      return null;
    } else {
      ProcessApplicationService processApplicationService = BpmPlatform.getProcessApplicationService();
      ProcessApplicationInfo processApplicationInfo = processApplicationService.getProcessApplicationInfo(processApplicationName);
      return processApplicationInfo.getProperties().get(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH);
    }
  }
}
