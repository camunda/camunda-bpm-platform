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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableListDto;
import org.camunda.bpm.engine.rest.dto.runtime.VariableValueDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.VariableResource;

public abstract class AbstractVariablesResource implements VariableResource {

  protected ProcessEngine engine;
  protected String resourceId;
  
  public AbstractVariablesResource(ProcessEngine engine, String resourceId) {
    this.engine = engine;
    this.resourceId = resourceId;
  }

  @Override
  public VariableListDto getVariables() {
    List<VariableValueDto> values = new ArrayList<VariableValueDto>();

    for (Map.Entry<String, Object> entry : getVariableEntities().entrySet()) {
      values.add(new VariableValueDto(entry.getKey(), entry.getValue(), entry.getValue().getClass().getSimpleName()));
    }

    return new VariableListDto(values);
  }

  @Override
  public VariableValueDto getVariable(String variableName) {
    Object variable = null;
    try {
       variable = getVariableEntity(variableName);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot get " + getResourceTypeName() + " variable " + variableName + ": " + e.getMessage());
    }
    
    if (variable == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, getResourceTypeName() + " variable with name " + variableName + " does not exist or is null");
    }
    
    return new VariableValueDto(variableName, variable, variable.getClass().getSimpleName());
    
  }

  @Override
  public void putVariable(String variableName, VariableValueDto variable) {
    
    try {
      setVariableEntity(variableName, variable.getValue());
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot put " + getResourceTypeName() + " variable " + variableName + ": " + e.getMessage());
    }
  }

  @Override
  public void deleteVariable(String variableName) {
    try {
      removeVariableEntity(variableName);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot delete " + getResourceTypeName() + " variable " + variableName + ": " + e.getMessage());
    }
  }
  
  @Override
  public void modifyVariables(PatchVariablesDto patch) {
    Map<String, Object> variableModifications = new HashMap<String, Object>();
    if (patch.getModifications() != null) {
      for (VariableValueDto variable : patch.getModifications()) {
        variableModifications.put(variable.getName(), variable.getValue());
      }
    }
    
    List<String> variableDeletions = patch.getDeletions();
    
    try {
      updateVariableEntities(variableModifications, variableDeletions);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, 
          "Cannot modify variables for " + getResourceTypeName() + " " + resourceId + ": " + e.getMessage());
    }
    
  }
  
  protected abstract Map<String, Object> getVariableEntities();
  
  protected abstract void updateVariableEntities(Map<String, Object> variables, List<String> deletions);
  
  protected abstract Object getVariableEntity(String variableKey);
  
  protected abstract void setVariableEntity(String variableKey, Object variableValue);
  
  protected abstract void removeVariableEntity(String variableKey);
  
  protected abstract String getResourceTypeName();
}
