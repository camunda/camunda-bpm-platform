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
package org.camunda.bpm.container.impl.jmx.services;

import org.activiti.engine.ProcessEngine;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanService;

/**
 * @author Daniel Meyer
 *
 */
public class JmxProcessEngine extends MBeanService<ProcessEngine> implements JmxProcessEngineMBean {
  
  protected ProcessEngine processEngine;
  protected boolean isDefault;
  
  public JmxProcessEngine(ProcessEngine processEngine, boolean isDefault) {
    this.processEngine = processEngine;
    this.isDefault = isDefault;
  }
  
  public void start(MBeanServiceContainer contanier) {
  }
  
  public void stop(MBeanServiceContainer container) {
  }
  
  public String getName() {
    return processEngine.getName();
  }

  public boolean isDefault() {
    return isDefault;
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
  
  public ProcessEngine getValue() {
    return processEngine;
  }

}
