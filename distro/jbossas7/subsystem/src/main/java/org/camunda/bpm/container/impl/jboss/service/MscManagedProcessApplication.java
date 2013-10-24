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

import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * <p>Represents a Process Application registered with the Msc</p>
 * 
 * <p>This is the equivalent of the {@link JmxManagedProcessApplication}</p>
 *  
 * @author Daniel Meyer
 *
 */
public class MscManagedProcessApplication implements Service<ProcessApplicationInfo> {
  
  protected ProcessApplicationInfo processApplicationInfo;

  public MscManagedProcessApplication(ProcessApplicationInfo processApplicationInfo) {
    this.processApplicationInfo = processApplicationInfo;
  }

  public ProcessApplicationInfo getValue() throws IllegalStateException, IllegalArgumentException {
    return processApplicationInfo;
  }
  
  public void start(StartContext context) throws StartException {
    // call the process application's 
  }
  
  public void stop(StopContext context) {
    // Nothing to do    
  }

}
