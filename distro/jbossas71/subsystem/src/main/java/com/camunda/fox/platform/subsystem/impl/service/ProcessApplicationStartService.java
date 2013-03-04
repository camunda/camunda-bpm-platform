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
package com.camunda.fox.platform.subsystem.impl.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationDeploymentInfo;
import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.application.impl.ProcessApplicationDeploymentInfoImpl;
import org.camunda.bpm.application.impl.ProcessApplicationInfoImpl;
import org.jboss.as.ee.component.ComponentView;
import org.jboss.as.naming.ManagedReference;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * <p>This service is responsible for starting the {@link MscManagedProcessApplication} service.</p>
 * 
 * <p>We need this as an extra step since we need a declarative dependency on the 
 * ProcessApplicationComponent in order to call the getName() method on the ProcessApplication. 
 * The name of the process application is subsequently used for composing the name of the 
 * {@link MscManagedProcessApplication} service which means that it must be available when 
 * registering the service.</p>  
 * 
 * <p>This service depends on all {@link ProcessApplicationDeploymentService} instances started for the 
 * process application. Thus, when this service is started, it is guaranteed that all process application 
 * deployments have completed successfully.</p>
 * 
 * <p>This service creates the {@link ProcessApplicationInfo} object and passes it to the 
 * {@link MscManagedProcessApplication} service.</p> 
 *  
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationStartService implements Service<ProcessApplicationStartService> {
  
  /** the names of the deployment services we depend on; those must be added as 
   * declarative dependencies when the service is installed. */
  protected final Collection<ServiceName> deploymentServiceNames;
  
  /** the process application component view */
  protected InjectedValue<ComponentView> processApplicationInjector = new InjectedValue<ComponentView>();
  
  public ProcessApplicationStartService(Collection<ServiceName> deploymentServiceNames) {
    this.deploymentServiceNames = deploymentServiceNames;
  }
  
  public void start(StartContext context) throws StartException {
    
    final ComponentView processApplicationComponentView = processApplicationInjector.getValue();
    
    ManagedReference reference = null;    
    try {
      
      reference = processApplicationComponentView.createInstance();      
      
      AbstractProcessApplication processApplication = (AbstractProcessApplication) reference.getInstance();
    
      // create & populate the process application info object
      ProcessApplicationInfoImpl processApplicationInfo = new ProcessApplicationInfoImpl();    
      processApplicationInfo.setName(processApplication.getName());    
      processApplicationInfo.setProperties(processApplication.getProperties());
            
      List<ProcessApplicationDeploymentInfo> deploymentInfos = new ArrayList<ProcessApplicationDeploymentInfo>();
       
      for (ServiceName deploymentServiceName : deploymentServiceNames) {
        
        ProcessApplicationDeploymentService value = getDeploymentService(context, deploymentServiceName);
              
        ProcessApplicationDeploymentInfoImpl deploymentInfo = new ProcessApplicationDeploymentInfoImpl();
        deploymentInfo.setDeploymentId(value.getDeployment().getId());
        deploymentInfo.setProcessEngineName(value.getProcessEngineName());
        deploymentInfo.setDeploymentName(value.getDeployment().getName());
        
        deploymentInfos.add(deploymentInfo);
              
      }
      processApplicationInfo.setDeploymentInfo(deploymentInfos);  
      
      // install the ManagedProcessApplication Service as a child to this service
      // if this service stops (at undeployment) the ManagedProcessApplication service is removed as well.
      ServiceName serviceName = ServiceNames.forManagedProcessApplication(processApplicationInfo.getName());
      MscManagedProcessApplication managedProcessApplication = new MscManagedProcessApplication(processApplicationInfo);
      context.getChildTarget().addService(serviceName, managedProcessApplication).install();
      
    } catch (Exception e) {
      throw new StartException(e);
      
    } finally {
      if(reference != null) {
        reference.release();
      }
      
    }    
  }
  
  public void stop(StopContext context) {
    
  }

  @SuppressWarnings("unchecked")
  private ProcessApplicationDeploymentService getDeploymentService(StartContext context, ServiceName deploymentServiceName) {
    final ServiceContainer serviceContainer = context.getController().getServiceContainer();
    ServiceController<ProcessApplicationDeploymentService> deploymentService = (ServiceController<ProcessApplicationDeploymentService>) serviceContainer.getRequiredService(deploymentServiceName);
    return deploymentService.getValue();
  }
    
  public InjectedValue<ComponentView> getProcessApplicationInjector() {
    return processApplicationInjector;
  } 

  public ProcessApplicationStartService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

}
