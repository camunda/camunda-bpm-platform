package org.camunda.bpm.engine.impl.context;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.InterpretableExecution;

public class ProcessApplicationContextUtil {

  public static ProcessApplicationReference getTargetProcessApplication(InterpretableExecution execution) {
    if (execution == null) {
      return null;
    }
    
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    
    String deploymentId = execution.getProcessDefinition().getDeploymentId();
    ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();
    
    return processApplicationManager.getProcessApplicationForDeployment(deploymentId);
  }
  
  public static boolean requiresContextSwitch(ProcessApplicationReference processApplicationReference) {
    
    final ProcessApplicationReference currentProcessApplication = Context.getCurrentProcessApplication();
    
    return processApplicationReference != null 
      && ( currentProcessApplication == null || !processApplicationReference.getName().equals(currentProcessApplication.getName()) );
    
  }
}
