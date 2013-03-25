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
package org.camunda.bpm.container.impl.ejb;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ProcessEngine;

/**
 * <p>Exposes the {@link ProcessEngineService} as EJB inside the container.</p>
 * 
 * @author Daniel Meyer
 *
 */
@Stateless(name="ProcessEngineService", mappedName="ProcessEngineService")
@Local(ProcessEngineService.class)
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class EjbProcessEngineService implements ProcessEngineService {
  
  @EJB
  protected EjbBpmPlatformBootstrap ejbBpmPlatform;
  
  /** the processEngineServiceDelegate */
  protected ProcessEngineService processEngineServiceDelegate;
  
  @PostConstruct
  protected void initProcessEngineServiceDelegate() {
    processEngineServiceDelegate = ejbBpmPlatform.getProcessEngineService();
  }

  public ProcessEngine getDefaultProcessEngine() {
    return processEngineServiceDelegate.getDefaultProcessEngine();
  }

  public List<ProcessEngine> getProcessEngines() {
    return processEngineServiceDelegate.getProcessEngines();
  }

  public Set<String> getProcessEngineNames() {
    return processEngineServiceDelegate.getProcessEngineNames();
  }

  public ProcessEngine getProcessEngine(String name) {
    return processEngineServiceDelegate.getProcessEngine(name);
  }

}
