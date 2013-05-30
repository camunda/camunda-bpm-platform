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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.rest.ExecutionRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableListDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;

public class ExecutionRestServiceImpl extends AbstractRestProcessEngineAware implements ExecutionRestService {

  public ExecutionRestServiceImpl() {
    super();
  }

  public ExecutionRestServiceImpl(String engineName) {
    super(engineName);
  }
  

  @Override
  public ExecutionDto getExecution(String executionId) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
    
    if (execution == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Execution with id " + executionId + " does not exist");
    }
    
    return ExecutionDto.fromExecution(execution);
  }
  
  @Override
  public List<ExecutionDto> getExecutions(UriInfo uriInfo, Integer firstResult,
      Integer maxResults) {
    ExecutionQueryDto queryDto = new ExecutionQueryDto(uriInfo.getQueryParameters());
    return queryExecutions(queryDto, firstResult, maxResults);
  }

  @Override
  public List<ExecutionDto> queryExecutions(
      ExecutionQueryDto queryDto, Integer firstResult, Integer maxResults) {
    ProcessEngine engine = getProcessEngine();
    ExecutionQuery query = queryDto.toQuery(engine);
    
    List<Execution> matchingExecutions;
    if (firstResult != null || maxResults != null) {
      matchingExecutions = executePaginatedQuery(query, firstResult, maxResults);
    } else {
      matchingExecutions = query.list();
    }
    
    List<ExecutionDto> executionResults = new ArrayList<ExecutionDto>();
    for (Execution execution : matchingExecutions) {
      ExecutionDto resultExecution = ExecutionDto.fromExecution(execution);
      executionResults.add(resultExecution);
    }
    return executionResults;
  }
  
  private List<Execution> executePaginatedQuery(ExecutionQuery query, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    return query.listPage(firstResult, maxResults); 
  }

  @Override
  public CountResultDto getExecutionsCount(UriInfo uriInfo) {
    ExecutionQueryDto queryDto = new ExecutionQueryDto(uriInfo.getQueryParameters());
    return queryExecutionsCount(queryDto);
  }

  @Override
  public CountResultDto queryExecutionsCount(ExecutionQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    ExecutionQuery query = queryDto.toQuery(engine);
    
    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);
    
    return result;
  }

  @Override
  public void signalExecution(String executionId,
      VariableListDto variables) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    try {
      runtimeService.signal(executionId, variables.toMap());
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot signal execution " + executionId + ": " + e.getMessage());
    }
    
  }
  
  @Override
  public VariableListDto getVariables(String executionId) {
    List<VariableValueDto> values = new ArrayList<VariableValueDto>();
    
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();

    for (Map.Entry<String, Object> entry : runtimeService.getVariablesLocal(executionId).entrySet()) {
      values.add(new VariableValueDto(entry.getKey(), entry.getValue(), entry.getValue().getClass().getSimpleName()));
    }

    return new VariableListDto(values);
  }

  @Override
  public void modifyVariables(String executionId, PatchVariablesDto patch) {
    Map<String, Object> variableModifications = new HashMap<String, Object>();
    if (patch.getModifications() != null) {
      for (VariableValueDto variable : patch.getModifications()) {
        variableModifications.put(variable.getName(), variable.getValue());
      }
    }
    
    List<String> variableDeletions = patch.getDeletions();
    RuntimeServiceImpl runtimeService = (RuntimeServiceImpl) getProcessEngine().getRuntimeService();
    
    try {
      runtimeService.updateVariablesLocal(executionId, variableModifications, variableDeletions);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, 
          "Cannot modify variables for execution " + executionId + ": " + e.getMessage());
    }
  }
  
  @Override
  public VariableValueDto getVariable(String executionId,
      String variableName) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    Object variable = null;
    try {
       variable = runtimeService.getVariableLocal(executionId, variableName);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot get execution variable " + variableName + ": " + e.getMessage());
    }
    
    if (variable == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Execution variable with name " + variableName + " does not exist or is null");
    }
    
    return new VariableValueDto(variableName, variable, variable.getClass().getSimpleName());
    
  }

  @Override
  public void putVariable(String executionId, String variableName,
      VariableValueDto variable) {
    
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    try {
      runtimeService.setVariableLocal(executionId, variableName, variable.getValue());
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot put execution variable " + variableName + ": " + e.getMessage());
    }
  }

  @Override
  public void deleteVariable(String executionId,
      String variableName) {
    RuntimeService runtimeService = getProcessEngine().getRuntimeService();
    try {
      runtimeService.removeVariableLocal(executionId, variableName);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot delete execution variable " + variableName + ": " + e.getMessage());
    }
  }
  
}
