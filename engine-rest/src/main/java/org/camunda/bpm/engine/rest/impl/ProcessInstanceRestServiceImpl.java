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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.DeleteEngineEntityDto;
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableListDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public class ProcessInstanceRestServiceImpl extends AbstractRestProcessEngineAware implements
    ProcessInstanceRestService {

  public ProcessInstanceRestServiceImpl() {
    super();
  }
  
  public ProcessInstanceRestServiceImpl(String engineName) {
    super(engineName);
  }
  

  @Override
  public ProcessInstanceDto getProcessInstance(String processInstanceId) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    
    if (instance == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Process instance with id " + processInstanceId + " does not exist");
    }
    
    ProcessInstanceDto result = ProcessInstanceDto.fromProcessInstance(instance);
    return result;
  }
  
  @Override
  public void deleteProcessInstance(String processInstanceId,
      DeleteEngineEntityDto dto) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    try {
      runtimeService.deleteProcessInstance(processInstanceId, dto.getDeleteReason());
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "Process instance with id " + processInstanceId + " does not exist");
    }
    
  }

  @Override
  public List<ProcessInstanceDto> getProcessInstances(
      UriInfo uriInfo, Integer firstResult, Integer maxResults) {
    ProcessInstanceQueryDto queryDto = new ProcessInstanceQueryDto(uriInfo.getQueryParameters());
    return queryProcessInstances(queryDto, firstResult, maxResults);
  }

  @Override
  public List<ProcessInstanceDto> queryProcessInstances(
      ProcessInstanceQueryDto queryDto, Integer firstResult, Integer maxResults) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    ProcessInstanceQuery query = queryDto.toQuery(runtimeService);
    
    List<ProcessInstance> matchingInstances;
    if (firstResult != null || maxResults != null) {
      matchingInstances = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingInstances = query.list();
    }
    
    List<ProcessInstanceDto> instanceResults = new ArrayList<ProcessInstanceDto>();
    for (ProcessInstance instance : matchingInstances) {
      ProcessInstanceDto resultInstance = ProcessInstanceDto.fromProcessInstance(instance);
      instanceResults.add(resultInstance);
    }
    return instanceResults;
  }
  
  private List<ProcessInstance> executePaginatedQuery(ProcessInstanceQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults); 
  }

  @Override
  public CountResultDto getProcessInstancesCount(UriInfo uriInfo) {
    ProcessInstanceQueryDto queryDto = new ProcessInstanceQueryDto(uriInfo.getQueryParameters());
    return queryProcessInstancesCount(queryDto);
  }

  @Override
  public CountResultDto queryProcessInstancesCount(ProcessInstanceQueryDto queryDto) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    ProcessInstanceQuery query = queryDto.toQuery(runtimeService);
    
    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    
    return result;
  }

  @Override
  public VariableListDto getVariables(String processInstanceId) {
    List<VariableValueDto> values = new ArrayList<VariableValueDto>();

    for (Map.Entry<String, Object> entry : getProcessEngine().getRuntimeService().getVariables(processInstanceId).entrySet()) {
      values.add(new VariableValueDto(entry.getKey(), entry.getValue(), entry.getValue().getClass().getSimpleName()));
    }

    return new VariableListDto(values);
  }

  @Override
  public VariableValueDto getVariable(String processInstanceId,
      String variableName) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    Object variable = null;
    try {
       variable = runtimeService.getVariable(processInstanceId, variableName);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot get variable " + variableName + ": " + e.getMessage());
    }
    
    if (variable == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Process variable with name " + variableName + " does not exist or is null");
    }
    
    return new VariableValueDto(variableName, variable, variable.getClass().getSimpleName());
    
  }

  @Override
  public void putVariable(String processInstanceId, String variableName,
      VariableValueDto variable) {
    
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    try {
      runtimeService.setVariable(processInstanceId, variableName, variable.getValue());
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot put variable " + variableName + ": " + e.getMessage());
    }
  }

  @Override
  public void deleteVariable(String processInstanceId,
      String variableName) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    try {
      runtimeService.removeVariable(processInstanceId, variableName);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot delete variable " + variableName + ": " + e.getMessage());
    }
  }
  
  @Override
  public void modifyVariables(String processInstanceId, PatchVariablesDto patch) {
    Map<String, Object> variableModifications = new HashMap<String, Object>();
    if (patch.getModifications() != null) {
      for (VariableValueDto variable : patch.getModifications()) {
        variableModifications.put(variable.getName(), variable.getValue());
      }
    }
    
    List<String> variableDeletions = patch.getDeletions();
    RuntimeServiceImpl runtimeService = (RuntimeServiceImpl) getProcessEngine().getRuntimeService();
    
    try {
      runtimeService.updateVariables(processInstanceId, variableModifications, variableDeletions);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, 
          "Cannot modify variables for process instance " + processInstanceId + ": " + e.getMessage());
    }
    
  }

  
}
