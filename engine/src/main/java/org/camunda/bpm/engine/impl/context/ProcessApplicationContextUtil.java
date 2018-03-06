package org.camunda.bpm.engine.impl.context;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.ProcessApplicationLogger;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;

import java.util.concurrent.Callable;

public class ProcessApplicationContextUtil {

  private final static ProcessApplicationLogger LOG = ProcessApplicationLogger.PROCESS_APPLICATION_LOGGER;

  public static ProcessApplicationReference getTargetProcessApplication(CoreExecution execution) {
    if (execution instanceof ExecutionEntity) {
      return getTargetProcessApplication((ExecutionEntity) execution);
    } else {
      return getTargetProcessApplication((CaseExecutionEntity) execution);
    }
  }

  public static ProcessApplicationReference getTargetProcessApplication(ExecutionEntity execution) {
    if (execution == null) {
      return null;
    }

    ProcessApplicationReference processApplicationForDeployment = getTargetProcessApplication((ProcessDefinitionEntity) execution.getProcessDefinition());

    // logg application context switch details
    if(LOG.isContextSwitchLoggable() && processApplicationForDeployment == null) {
      loggContextSwitchDetails(execution);
    }

    return processApplicationForDeployment;
  }

  public static ProcessApplicationReference getTargetProcessApplication(CaseExecutionEntity execution) {
    if (execution == null) {
      return null;
    }

    ProcessApplicationReference processApplicationForDeployment = getTargetProcessApplication((CaseDefinitionEntity) execution.getCaseDefinition());

    // logg application context switch details
    if(LOG.isContextSwitchLoggable() && processApplicationForDeployment == null) {
      loggContextSwitchDetails(execution);
    }

    return processApplicationForDeployment;
  }

  public static ProcessApplicationReference getTargetProcessApplication(TaskEntity task) {
    if (task.getProcessDefinition() != null) {
      return getTargetProcessApplication(task.getProcessDefinition());
    }
    else if (task.getCaseDefinition() != null) {
      return getTargetProcessApplication(task.getCaseDefinition());
    }
    else {
      return null;
    }
  }

  public static ProcessApplicationReference getTargetProcessApplication(ResourceDefinitionEntity definition) {
    ProcessApplicationReference reference = getTargetProcessApplication(definition.getDeploymentId());

    if (reference == null && areProcessApplicationsRegistered()) {
      ResourceDefinitionEntity previous = definition.getPreviousDefinition();

      // do it in a iterative way instead of recursive to avoid
      // a possible StackOverflowException in cases with a lot
      // of versions of a definition
      while (previous != null) {
        reference = getTargetProcessApplication(previous.getDeploymentId());

        if (reference == null) {
          previous = previous.getPreviousDefinition();
        }
        else {
          return reference;
        }

      }
    }

    return reference;
  }

  public static ProcessApplicationReference getTargetProcessApplication(String deploymentId) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();

    ProcessApplicationReference processApplicationForDeployment = processApplicationManager.getProcessApplicationForDeployment(deploymentId);

    return processApplicationForDeployment;
  }

  public static boolean areProcessApplicationsRegistered() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();

    return processApplicationManager.hasRegistrations();
  }

  private static void loggContextSwitchDetails(ExecutionEntity execution) {

    final CoreExecutionContext<? extends CoreExecution> executionContext = Context.getCoreExecutionContext();
    // only log for first atomic op:
    if(executionContext == null ||( executionContext.getExecution() != execution) ) {
      ProcessApplicationManager processApplicationManager = Context.getProcessEngineConfiguration().getProcessApplicationManager();
      LOG.debugNoTargetProcessApplicationFound(execution, processApplicationManager);
    }

  }

  private static void loggContextSwitchDetails(CaseExecutionEntity execution) {

    final CoreExecutionContext<? extends CoreExecution> executionContext = Context.getCoreExecutionContext();
    // only log for first atomic op:
    if(executionContext == null ||( executionContext.getExecution() != execution) ) {
      ProcessApplicationManager processApplicationManager = Context.getProcessEngineConfiguration().getProcessApplicationManager();
      LOG.debugNoTargetProcessApplicationFoundForCaseExecution(execution, processApplicationManager);
    }

  }

  public static boolean requiresContextSwitch(ProcessApplicationReference processApplicationReference) {

    final ProcessApplicationReference currentProcessApplication = Context.getCurrentProcessApplication();

    if(processApplicationReference == null) {
      return false;
    }

    if(currentProcessApplication == null) {
      return true;
    }
    else {
      if(!processApplicationReference.getName().equals(currentProcessApplication.getName())) {
        return true;
      }
      else {
        // check whether the thread context has been manipulated since last context switch. This can happen as a result of
        // an operation causing the container to switch to a different application.
        // Example: JavaDelegate implementation (inside PA) invokes an EJB from different application which in turn interacts with the Process engine.
        ClassLoader processApplicationClassLoader = ProcessApplicationClassloaderInterceptor.getProcessApplicationClassLoader();
        ClassLoader currentClassloader = ClassLoaderUtil.getContextClassloader();
        return currentClassloader != processApplicationClassLoader;
      }
    }
  }

  public static void doContextSwitch(final Runnable runnable, ProcessDefinitionEntity contextDefinition) {
    ProcessApplicationReference processApplication = getTargetProcessApplication(contextDefinition);
    if (requiresContextSwitch(processApplication)) {
      Context.executeWithinProcessApplication(new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          runnable.run();
          return null;
        }
      }, processApplication);
    }
    else {
      runnable.run();
    }
  }
}
