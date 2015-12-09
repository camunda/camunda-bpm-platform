package org.camunda.bpm.container.impl.ejb;

import org.camunda.bpm.ProcessApplicationService;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.container.ExecutorService;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.container.impl.RuntimeContainerDelegateImpl;
import org.camunda.bpm.container.impl.deployment.DiscoverBpmPlatformPluginsStep;
import org.camunda.bpm.container.impl.deployment.PlatformXmlStartProcessEnginesStep;
import org.camunda.bpm.container.impl.deployment.StopProcessApplicationsStep;
import org.camunda.bpm.container.impl.deployment.StopProcessEnginesStep;
import org.camunda.bpm.container.impl.deployment.UnregisterBpmPlatformPluginsStep;
import org.camunda.bpm.container.impl.deployment.jobexecutor.StartJobExecutorStep;
import org.camunda.bpm.container.impl.deployment.jobexecutor.StopJobExecutorStep;
import org.camunda.bpm.container.impl.ejb.deployment.EjbJarParsePlatformXmlStep;
import org.camunda.bpm.container.impl.ejb.deployment.StartJcaExecutorServiceStep;
import org.camunda.bpm.container.impl.ejb.deployment.StopJcaExecutorServiceStep;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>Bootstrap for the camunda BPM platform using a singleton EJB</p>
 *
 * @author Daniel Meyer
 */
@Startup
@Singleton(name="BpmPlatformBootstrap")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class EjbBpmPlatformBootstrap {

  final private static Logger LOGGER = Logger.getLogger(EjbBpmPlatformBootstrap.class.getName());

  @EJB
  protected ExecutorService executorServiceBean;

  protected ProcessEngineService processEngineService;
  protected ProcessApplicationService processApplicationService;

  @PostConstruct
  protected void start() {

    final RuntimeContainerDelegateImpl containerDelegate = getContainerDelegate();

    containerDelegate.getServiceContainer().createDeploymentOperation("deploying camunda BPM platform")
      .addStep(new EjbJarParsePlatformXmlStep())
      .addStep(new DiscoverBpmPlatformPluginsStep())
      .addStep(new StartJcaExecutorServiceStep(executorServiceBean))
      .addStep(new StartJobExecutorStep())
      .addStep(new PlatformXmlStartProcessEnginesStep())
      .execute();

    processEngineService = containerDelegate.getProcessEngineService();
    processApplicationService = containerDelegate.getProcessApplicationService();

    LOGGER.log(Level.INFO, "camunda BPM platform started successfully.");
  }

  @PreDestroy
  protected void stop() {

    final RuntimeContainerDelegateImpl containerDelegate = getContainerDelegate();

    containerDelegate.getServiceContainer().createUndeploymentOperation("undeploying camunda BPM platform")
      .addStep(new StopProcessApplicationsStep())
      .addStep(new StopProcessEnginesStep())
      .addStep(new StopJobExecutorStep())
      .addStep(new StopJcaExecutorServiceStep())
      .addStep(new UnregisterBpmPlatformPluginsStep())
      .execute();

    LOGGER.log(Level.INFO, "camunda BPM platform stopped.");

  }

  protected RuntimeContainerDelegateImpl getContainerDelegate() {
    return (RuntimeContainerDelegateImpl) RuntimeContainerDelegate.INSTANCE.get();
  }

  // getters //////////////////////////////////////////////

  public ProcessEngineService getProcessEngineService() {
    return processEngineService;
  }

  public ProcessApplicationService getProcessApplicationService() {
    return processApplicationService;
  }

}
