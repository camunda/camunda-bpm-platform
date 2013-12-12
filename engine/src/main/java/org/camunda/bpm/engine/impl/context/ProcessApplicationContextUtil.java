package org.camunda.bpm.engine.impl.context;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.InterpretableExecution;

public class ProcessApplicationContextUtil {

  private static final Logger LOGG = Logger.getLogger(ProcessApplicationContextUtil.class.getName());

  public static ProcessApplicationReference getTargetProcessApplication(InterpretableExecution execution) {
    if (execution == null) {
      return null;
    }

    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    String deploymentId = execution.getProcessDefinition().getDeploymentId();
    ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();

    ProcessApplicationReference processApplicationForDeployment = processApplicationManager.getProcessApplicationForDeployment(deploymentId);

    // logg application context switch details
    if(LOGG.isLoggable(Level.FINE) && processApplicationForDeployment == null) {
      loggContextSwitchDetails(execution, processApplicationManager);
    }

    return processApplicationForDeployment;
  }

  private static void loggContextSwitchDetails(InterpretableExecution execution, ProcessApplicationManager processApplicationManager) {

    final ExecutionContext executionContext = Context.getExecutionContext();
    // only log for first atomic op:
    if(executionContext == null ||( executionContext.getExecution() != execution) ) {
      LOGG.log(Level.FINE,
        String.format("[PA-CONTEXT] no target process application found for Execution[%s], ProcessDefinition[%s], Deployment[%s] Registrations[%s]",
            execution.getId(),
            execution.getProcessDefinitionId(),
            execution.getProcessDefinition().getDeploymentId(),
            processApplicationManager.getRegistrationSummary()));
    }

  }

  public static boolean requiresContextSwitch(ProcessApplicationReference processApplicationReference) {

    final ProcessApplicationReference currentProcessApplication = Context.getCurrentProcessApplication();

    return processApplicationReference != null
      && ( currentProcessApplication == null || !processApplicationReference.getName().equals(currentProcessApplication.getName()) );

  }
}
