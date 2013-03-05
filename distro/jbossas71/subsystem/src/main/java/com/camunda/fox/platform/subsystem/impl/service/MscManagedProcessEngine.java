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
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngine;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

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
  
  /** the process engine managed by this service */
  protected ProcessEngine processEngine;

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
    // no lifecycle
  }

  public void stop(StopContext context) {
    // no lifecycle    
  }
  
}
