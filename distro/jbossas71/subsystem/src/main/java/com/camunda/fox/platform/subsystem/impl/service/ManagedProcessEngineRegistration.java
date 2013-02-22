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

import org.activiti.engine.ProcessEngine;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import com.camunda.fox.platform.subsystem.impl.extension.FoxPlatformExtension;

/**
 * <p>Service representing a managed process engine instance registered with the Msc.</p>
 * 
 * <p>Instances of this service are created and registered by the {@link MscRuntimeContainerDelegate} 
 * when {@link MscRuntimeContainerDelegate#registerProcessEngine(ProcessEngine)} is called, if the 
 * process engine is managed by an application and not started by the container infrastructure.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ManagedProcessEngineRegistration implements Service<ProcessEngine> {
  
  /** the process engine managed by this service */
  protected ProcessEngine processEngine;
  
  public ManagedProcessEngineRegistration(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  public ProcessEngine getValue() throws IllegalStateException, IllegalArgumentException {
    return processEngine;
  }

  public void start(StartContext context) throws StartException {
    // nothing to do
  }

  public void stop(StopContext context) {
    // nothing to do    
  }
  
  public static ServiceName getServiceName(String processEngineName) {
    return getServiceType().append(processEngineName);
  }

  private static ServiceName getServiceType() {
    return FoxPlatformExtension.getPlatformServiceType().append("process-engine");
  }

}
