package org.camunda.bpm.engine.rest.util;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;

public class ApplicationContextPathUtil {

  public static String getApplicationPath(ProcessEngine engine, String processDefinitionId) {
    ProcessDefinition processDefinition = engine.getRepositoryService().getProcessDefinition(processDefinitionId);
    
    if (processDefinition == null) {
      return null;
    }

    if(processDefinition == null) {
      return null;
    }
    
    // get the name of the process application that made the deployment
    String processApplicationName = engine.getManagementService().getProcessApplicationForDeployment(processDefinition.getDeploymentId());

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
