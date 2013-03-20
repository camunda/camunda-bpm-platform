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
package org.camunda.bpm.engine.rest.spi.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public class MockedProcessEngineProvider implements ProcessEngineProvider {

  private static ProcessEngine cachedDefaultProcessEngine;
  private static Map<String, ProcessEngine> cachedEngines = new HashMap<String, ProcessEngine>();
  
  public static final String EXAMPLE_PROCESS_ENGINE_NAME = "default";
  public static final String ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME = "anotherEngineName";
  public static final String NON_EXISTING_PROCESS_ENGINE_NAME = "aNonExistingEngineName";
  
  public void resetEngines() {
    cachedDefaultProcessEngine = null;
    cachedEngines = new HashMap<String, ProcessEngine>();
  }
  
  private void mockServices(ProcessEngine engine) {
    RepositoryService repoService = mock(RepositoryService.class);
    IdentityService identityService = mock(IdentityService.class);
    TaskService taskService = mock(TaskService.class);
    RuntimeService runtimeService = mock(RuntimeService.class);
    FormService formService = mock(FormService.class);
    HistoryService historyService = mock(HistoryService.class);
    ManagementService managementService = mock(ManagementService.class);
    
    when(engine.getRepositoryService()).thenReturn(repoService);
    when(engine.getIdentityService()).thenReturn(identityService);
    when(engine.getTaskService()).thenReturn(taskService);
    when(engine.getRuntimeService()).thenReturn(runtimeService);
    when(engine.getFormService()).thenReturn(formService);
    when(engine.getHistoryService()).thenReturn(historyService);
    when(engine.getManagementService()).thenReturn(managementService);
  }

  @Override
  public ProcessEngine getDefaultProcessEngine() {
    if (cachedDefaultProcessEngine == null) {
      cachedDefaultProcessEngine = mock(ProcessEngine.class);
      mockServices(cachedDefaultProcessEngine);
    }
    
    return cachedDefaultProcessEngine;
  }

  @Override
  public ProcessEngine getProcessEngine(String name) {
    if (name.equals(NON_EXISTING_PROCESS_ENGINE_NAME)) {
      return null;
    }
    
    if (name.equals("default")) {
      return getDefaultProcessEngine();
    }
    
    if (cachedEngines.get(name) == null) {
      ProcessEngine mock = mock(ProcessEngine.class);
      mockServices(mock);
      cachedEngines.put(name, mock);
    }
    
    return cachedEngines.get(name);
  }

  @Override
  public Set<String> getProcessEngineNames() {
    Set<String> result = new HashSet<String>();
    result.add(EXAMPLE_PROCESS_ENGINE_NAME);
    result.add(ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME);
    return result;
  }
  

}
