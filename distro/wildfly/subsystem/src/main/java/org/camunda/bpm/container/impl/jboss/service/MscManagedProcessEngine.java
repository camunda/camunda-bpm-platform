/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.service;

import java.util.logging.Logger;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.impl.jboss.util.BindingUtil;
import org.camunda.bpm.container.impl.jboss.util.ProcessEngineManagedReferenceFactory;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngine;
import org.camunda.bpm.engine.ProcessEngine;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * <p>Service representing a managed process engine instance registered with the Msc.</p>
 * 
 * <p>Instances of this service are created and registered by the {@link MscRuntimeContainerDelegate} 
 * when {@link MscRuntimeContainerDelegate#registerProcessEngine(ProcessEngine)} is called.</p>
 * 
 * <p>This is the JBoass Msc counterpart of the {@link JmxManagedProcessEngine}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class MscManagedProcessEngine implements Service<ProcessEngine> {
  
  private final static Logger LOGG = Logger.getLogger(MscManagedProcessEngine.class.getName());
  
  protected InjectedValue<MscRuntimeContainerDelegate> runtimeContainerDelegateInjector = new InjectedValue<MscRuntimeContainerDelegate>();
  
  /** the process engine managed by this service */
  protected ProcessEngine processEngine;

  private ServiceController<ManagedReferenceFactory> bindingService;

  // for subclasses only
  protected MscManagedProcessEngine() {
  }
  
  public MscManagedProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  public ProcessEngine getValue() throws IllegalStateException, IllegalArgumentException {
    return processEngine;
  }

  public void start(StartContext context) throws StartException {
    MscRuntimeContainerDelegate runtimeContainerDelegate = runtimeContainerDelegateInjector.getValue();
    runtimeContainerDelegate.processEngineStarted(processEngine);
    
    createProcessEngineJndiBinding(context);
  }

  protected void createProcessEngineJndiBinding(StartContext context) {
    
    final ProcessEngineManagedReferenceFactory managedReferenceFactory = new ProcessEngineManagedReferenceFactory(processEngine);
    
    final ServiceName processEngineServiceBindingServiceName = ContextNames.GLOBAL_CONTEXT_SERVICE_NAME            
        .append(BpmPlatform.APP_JNDI_NAME)
        .append(BpmPlatform.MODULE_JNDI_NAME)
        .append(processEngine.getName());
    
    final String jndiName = BpmPlatform.JNDI_NAME_PREFIX 
        + "/" + BpmPlatform.APP_JNDI_NAME 
        + "/" + BpmPlatform.MODULE_JNDI_NAME 
        + "/" +processEngine.getName();

    // bind process engine service
    bindingService = BindingUtil.createJndiBindings(context.getChildTarget(), processEngineServiceBindingServiceName, jndiName, managedReferenceFactory);

    // log info message
    LOGG.info("jndi binding for process engine " + processEngine.getName() + " is " + jndiName);
  }
  
  protected void removeProcessEngineJndiBinding() {
    bindingService.setMode(Mode.REMOVE);
  }
  
  public void stop(StopContext context) {
    MscRuntimeContainerDelegate runtimeContainerDelegate = runtimeContainerDelegateInjector.getValue();
    runtimeContainerDelegate.processEngineStopped(processEngine);  
  }

  public Injector<MscRuntimeContainerDelegate> getRuntimeContainerDelegateInjector() {
    return runtimeContainerDelegateInjector;
  }
  
}
