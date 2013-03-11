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
package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.rest.ProcessEngineRestService;
import org.camunda.bpm.engine.rest.ProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.dto.ProcessEngineDto;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

public class ProcessEngineRestServiceImpl implements ProcessEngineRestService {

  @Override
  public ProcessDefinitionRestService getProcessDefinitionService(String engineName) {
    return new ProcessDefinitionRestServiceImpl(engineName);
  }

  @Override
  public ProcessInstanceRestService getProcessInstanceService(String engineName) {
    return new ProcessInstanceRestServiceImpl(engineName);
  }

  @Override
  public TaskRestService getTaskRestService(String engineName) {
    return new TaskRestServiceImpl(engineName);
  }

  @Override
  public List<ProcessEngineDto> getProcessEngineNames() {
    ProcessEngineProvider provider = getProcessEngineProvider();
    Set<String> engineNames = provider.getProcessEngineNames();
    
    List<ProcessEngineDto> results = new ArrayList<ProcessEngineDto>();
    for (String engineName : engineNames) {
      ProcessEngineDto dto = new ProcessEngineDto();
      dto.setName(engineName);
      results.add(dto);
    }
    
    return results;
  }
  
  
  private ProcessEngineProvider getProcessEngineProvider() {
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();
    
    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider;
    } else {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }
}
