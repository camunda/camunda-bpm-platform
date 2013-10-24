/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.service;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.jboss.as.ee.component.ComponentView;
import org.jboss.as.naming.ManagedReference;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * <p>Service responsible for performing a deployment to the process engine and managing
 * the resulting {@link ProcessApplicationRegistration} with the process engine.</p>
 *
 * <p>We construct one of these per Process Archive of a Process Application.</p>
 *
 * <p>We need a dependency on the componentView service of the ProcessApplication
 * component and the process engine to which the deployment should be performed.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationDeploymentService implements Service<ProcessApplicationDeploymentService> {

  private final static Logger LOGGER = Logger.getLogger(ProcessApplicationDeploymentService.class.getName());

  protected InjectedValue<ExecutorService> executorInjector = new InjectedValue<ExecutorService>();

  protected InjectedValue<ProcessEngine> processEngineInjector = new InjectedValue<ProcessEngine>();

  protected InjectedValue<ProcessApplicationInterface> noViewProcessApplication = new InjectedValue<ProcessApplicationInterface>();
  // for view-exposing ProcessApplicationComponents
  protected InjectedValue<ComponentView> paComponentViewInjector = new InjectedValue<ComponentView>();

  /** the map of deployment resources obtained  through scanning */
  protected final Map<String,byte[]> deploymentMap;
  /** deployment metadata that is passed in */
  protected final ProcessArchiveXml processArchive;

  /** the deployment we create here */
  protected ProcessApplicationDeployment deployment;

  public ProcessApplicationDeploymentService(Map<String,byte[]> deploymentMap, ProcessArchiveXml processArchive) {
    this.deploymentMap = deploymentMap;
    this.processArchive = processArchive;
  }

  public void start(final StartContext context) throws StartException {
    context.asynchronous();
    executorInjector.getValue().submit(new Runnable() {
      public void run() {
        try {
          performDeployment();
          context.complete();
        } catch (StartException e) {
          context.failed(e);
        } catch (Throwable e) {
          context.failed(new StartException(e));
        }
      }
    });
  }

  public void stop(final StopContext context) {
    context.asynchronous();
    executorInjector.getValue().submit(new Runnable() {
      public void run() {
        try {
          performUndeployment();
        } finally {
          context.complete();
        }
      }
    });
  }

  protected void performDeployment() throws StartException {

    ManagedReference reference = null;
    try {

      // get process engine
      ProcessEngine processEngine = processEngineInjector.getValue();

      // get the process application component
      ProcessApplicationInterface processApplication = null;
      ComponentView componentView = paComponentViewInjector.getOptionalValue();
      if(componentView != null) {
        reference = componentView.createInstance();
        processApplication = (ProcessApplicationInterface) reference.getInstance();
      } else {
        processApplication = noViewProcessApplication.getValue();
      }

      // get the application name
      String processApplicationName = processApplication.getName();

      // build the deployment
      final RepositoryService repositoryService = processEngine.getRepositoryService();

      ProcessApplicationDeploymentBuilder deploymentBuilder = repositoryService.createDeployment(processApplication.getReference());

      // enable duplicate filtering
      deploymentBuilder.enableDuplicateFiltering();

      // enable resuming of previous versions:
      if(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_RESUME_PREVIOUS_VERSIONS, true)) {
        deploymentBuilder.resumePreviousVersions();
      }

      // set the name for the deployment
      String deploymentName = processArchive.getName();
      if(deploymentName == null || deploymentName.isEmpty()) {
        deploymentName = processApplicationName;
      }
      deploymentBuilder.name(deploymentName);
      logDeploymentSummary(deploymentMap, deploymentName, processApplicationName);

      // add deployment resources
      for (Entry<String, byte[]> resource : deploymentMap.entrySet()) {
        deploymentBuilder.addInputStream(resource.getKey(), new ByteArrayInputStream(resource.getValue()));
      }

      // let the process application component add resources to the deployment.
      processApplication.createDeployment(processArchive.getName(), deploymentBuilder);

      // perform the actual deployment
      deployment = deploymentBuilder.deploy();

    } catch (Exception e) {
      throw new StartException("Could not register process application with shared process engine ",e);

    } finally {
      if(reference != null) {
        reference.release();
      }
    }
  }

  /**
   * @param deploymentMap2
   * @param deploymentName
   */
  protected void logDeploymentSummary(Map<String, byte[]> deploymentMap, String deploymentName, String processApplicationName) {
    // log a summary of the deployment
    if(!deploymentMap.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append("Deployment summary for process archive '"+deploymentName+"' of process application '"+processApplicationName+"': \n");
      builder.append("\n");
      for (String resourceName : deploymentMap.keySet()) {
        builder.append("        "+resourceName);
        builder.append("\n");
      }
      LOGGER.log(Level.INFO, builder.toString());

    } else {
      LOGGER.info("process archive '"+deploymentName+"'"
          +" of process application '"+processApplicationName+"'"
          +" does provide any deployment resources.");

    }
  }

  protected void performUndeployment() {

    final ProcessEngine processEngine = processEngineInjector.getValue();

    try {
      // always unregister
      Set<String> deploymentIds = deployment.getProcessApplicationRegistration().getDeploymentIds();
      processEngine.getManagementService().unregisterProcessApplication(deploymentIds, true);
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Exception while unregistering process application with the process engine.");

    }

    // delete the deployment only if requested in metadata
    if(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_DELETE_UPON_UNDEPLOY, false)) {
      try {
        LOGGER.info("Deleting cascade deployment with name '"+deployment.getName()+"/"+deployment.getId()+"'.");
        processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);

      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Exception while deleting process engine deployment", e);

      }

    }
  }

  public ProcessApplicationDeploymentService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  public InjectedValue<ProcessEngine> getProcessEngineInjector() {
    return processEngineInjector;
  }

  public InjectedValue<ProcessApplicationInterface> getNoViewProcessApplication() {
    return noViewProcessApplication;
  }

  public InjectedValue<ComponentView> getPaComponentViewInjector() {
    return paComponentViewInjector;
  }

  public ProcessApplicationDeployment getDeployment() {
    return deployment;
  }

  public String getProcessEngineName() {
    return processEngineInjector.getValue().getName();
  }

  public InjectedValue<ExecutorService> getExecutorInjector() {
    return executorInjector;
  }


}
