package org.camunda.bpm.engine.impl.context;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

public class ProcessApplicationContextUtil {

  private static final Logger LOGG = Logger.getLogger(ProcessApplicationContextUtil.class.getName());

  public static ProcessApplicationReference getTargetProcessApplication(ExecutionEntity execution) {
    if (execution == null) {
      return null;
    }

    ProcessApplicationReference processApplicationForDeployment = getTargetProcessApplication(execution.getProcessDefinition().getDeploymentId());

    // logg application context switch details
    if(LOGG.isLoggable(Level.FINE) && processApplicationForDeployment == null) {
      loggContextSwitchDetails(execution);
    }

    return processApplicationForDeployment;
  }

  public static ProcessApplicationReference getTargetProcessApplication(CaseExecutionEntity execution) {
    if (execution == null) {
      return null;
    }

    ProcessApplicationReference processApplicationForDeployment = getTargetProcessApplication(((CaseDefinitionEntity) execution.getCaseDefinition()).getDeploymentId());

    // logg application context switch details
    if(LOGG.isLoggable(Level.FINE) && processApplicationForDeployment == null) {
      loggContextSwitchDetails(execution);
    }

    return processApplicationForDeployment;
  }

  public static ProcessApplicationReference getTargetProcessApplication(String deploymentId) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();

    ProcessApplicationReference processApplicationForDeployment = processApplicationManager.getProcessApplicationForDeployment(deploymentId);

    return processApplicationForDeployment;
  }

  private static void loggContextSwitchDetails(ExecutionEntity execution) {

    final CoreExecutionContext<? extends CoreExecution> executionContext = Context.getCoreExecutionContext();
    // only log for first atomic op:
    if(executionContext == null ||( executionContext.getExecution() != execution) ) {
      ProcessApplicationManager processApplicationManager = Context.getProcessEngineConfiguration().getProcessApplicationManager();
      LOGG.log(Level.FINE,
        String.format("[PA-CONTEXT] no target process application found for Execution[%s], ProcessDefinition[%s], Deployment[%s] Registrations[%s]",
            execution.getId(),
            execution.getProcessDefinitionId(),
            execution.getProcessDefinition().getDeploymentId(),
            processApplicationManager.getRegistrationSummary()));
    }

  }

  private static void loggContextSwitchDetails(CaseExecutionEntity execution) {

    final CoreExecutionContext<? extends CoreExecution> executionContext = Context.getCoreExecutionContext();
    // only log for first atomic op:
    if(executionContext == null ||( executionContext.getExecution() != execution) ) {
      ProcessApplicationManager processApplicationManager = Context.getProcessEngineConfiguration().getProcessApplicationManager();
      LOGG.log(Level.FINE,
        String.format("[PA-CONTEXT] no target process application found for CaseExecution[%s], CaseDefinition[%s], Deployment[%s] Registrations[%s]",
            execution.getId(),
            execution.getCaseDefinitionId(),
            ((CaseDefinitionEntity) execution.getCaseDefinition()).getDeploymentId(),
            processApplicationManager.getRegistrationSummary()));
    }

  }

  public static boolean requiresContextSwitch(ProcessApplicationReference processApplicationReference) {

    final ProcessApplicationReference currentProcessApplication = Context.getCurrentProcessApplication();

    return processApplicationReference != null
      && ( currentProcessApplication == null || !processApplicationReference.getName().equals(currentProcessApplication.getName()) );

  }
}
