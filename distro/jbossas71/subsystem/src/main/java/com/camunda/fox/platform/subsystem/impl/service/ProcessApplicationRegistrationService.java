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

import org.activiti.engine.ManagementService;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.engine.application.ProcessApplicationRegistration;
import org.jboss.as.ee.component.ComponentView;
import org.jboss.as.naming.ManagedReference;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * <p>Service responsible for managing a registration of a process application with
 * a process engine.
 * 
 * <p>We construct one of these for each deployment performed by the process
 * application through the container infrastructure.
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessApplicationRegistrationService implements Service<ProcessApplicationRegistrationService> {
  
  protected InjectedValue<ContainerProcessEngineController> processEngineInjector = new InjectedValue<ContainerProcessEngineController>();
  protected InjectedValue<ComponentView> processApplicationInjector = new InjectedValue<ComponentView>();
  
  protected final String deploymentId;
  protected ProcessApplicationRegistration registration;
  
  public ProcessApplicationRegistrationService(String deploymentId) {
    this.deploymentId = deploymentId;    
  }

  public ProcessApplicationRegistrationService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  public void start(StartContext context) throws StartException {
    
    final ContainerProcessEngineController processEngineController = processEngineInjector.getValue();
    final ComponentView processApplicationComponentView = processApplicationInjector.getValue();
    
    ManagedReference reference = null;    
    try {
      
      reference = processApplicationComponentView.createInstance();      
      
      ProcessApplication processApplication = (ProcessApplication) reference.getInstance();      
      ManagementService managementService = processEngineController.getProcessEngine().getManagementService();      
      registration = managementService.activateDeploymentForApplication(deploymentId, processApplication.getReference());
      
    } catch (Exception e) {
      throw new StartException("Could not register process application with shared process engine ",e);
      
    } finally {
      if(reference != null) {
        reference.release();
      }
      
    }
    
  }

  public void stop(StopContext context) {
    registration.unregister();
  }

  public ServiceName getServiceName() {
    return ContainerProcessEngineService.getServiceName().append("process-application-registration").append(deploymentId);
  }
  
  public InjectedValue<ContainerProcessEngineController> getProcessEngineInjector() {
    return processEngineInjector;
  }
  
  public InjectedValue<ComponentView> getProcessApplicationInjector() {
    return processApplicationInjector;
  }

}
