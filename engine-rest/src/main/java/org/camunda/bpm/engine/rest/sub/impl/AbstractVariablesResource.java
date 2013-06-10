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
package org.camunda.bpm.engine.rest.sub.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.rest.util.DtoUtil;

public abstract class AbstractVariablesResource implements VariableResource {

  protected ProcessEngine engine;
  protected String resourceId;
  
  public AbstractVariablesResource(ProcessEngine engine, String resourceId) {
    this.engine = engine;
    this.resourceId = resourceId;
  }

  @Override
  public Map<String, VariableValueDto> getVariables() {
    Map<String, VariableValueDto> values = new HashMap<String, VariableValueDto>();

    for (Map.Entry<String, Object> entry : getVariableEntities().entrySet()) {
      values.put(entry.getKey(), new VariableValueDto(entry.getValue(), entry.getValue().getClass().getSimpleName()));
    }

    return values;
  }

  @Override
  public VariableValueDto getVariable(String variableName) {
    Object variable = null;
    try {
       variable = getVariableEntity(variableName);
    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot get %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }
    
    if (variable == null) {
      String errorMessage = String.format("%s variable with name %s does not exist or is null", getResourceTypeName(), variableName);
      throw new InvalidRequestException(Status.NOT_FOUND, errorMessage);
    }
    
    return new VariableValueDto(variable, variable.getClass().getSimpleName());
    
  }

  @Override
  public void putVariable(String variableName, VariableValueDto variable) {
    
    try {
      setVariableEntity(variableName, variable.getValue());
    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot put %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }
  }

  @Override
  public void deleteVariable(String variableName) {
    try {
      removeVariableEntity(variableName);
    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot delete %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }
  }
  
  @Override
  public void modifyVariables(PatchVariablesDto patch) {
    Map<String, Object> variableModifications = DtoUtil.toMap(patch.getModifications());
    
    List<String> variableDeletions = patch.getDeletions();
    
    try {
      updateVariableEntities(variableModifications, variableDeletions);
    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot modify variables for %s %s: %s", getResourceTypeName(), resourceId, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }
    
  }
  
  protected abstract Map<String, Object> getVariableEntities();
  
  protected abstract void updateVariableEntities(Map<String, Object> variables, List<String> deletions);
  
  protected abstract Object getVariableEntity(String variableKey);
  
  protected abstract void setVariableEntity(String variableKey, Object variableValue);
  
  protected abstract void removeVariableEntity(String variableKey);
  
  protected abstract String getResourceTypeName();
}
