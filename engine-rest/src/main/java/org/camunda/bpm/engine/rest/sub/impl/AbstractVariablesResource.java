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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.variable.BooleanType;
import org.camunda.bpm.engine.impl.variable.DateType;
import org.camunda.bpm.engine.impl.variable.DoubleType;
import org.camunda.bpm.engine.impl.variable.IntegerType;
import org.camunda.bpm.engine.impl.variable.LongType;
import org.camunda.bpm.engine.impl.variable.ShortType;
import org.camunda.bpm.engine.impl.variable.StringType;
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
      String key = entry.getKey();
      Object value = entry.getValue();
      String simpleClassName = null;
      
      if (value != null) {
        // if the value is not equals null, then get the simple class name.
        simpleClassName = value.getClass().getSimpleName();
      } else {
        // if the value is equals null, then the simple class name is "Null".
        simpleClassName = "Null";
      }
      
      values.put(key, new VariableValueDto(value, simpleClassName));
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
      String type = variable.getType();
      Object value = variable.getValue();
      
      if (type != null && !type.equals("") && value != null) {
        
        // boolean
        if (type.equalsIgnoreCase(BooleanType.TYPE_NAME)) {
          setVariableEntity(variableName, Boolean.valueOf(value.toString()));
          return;
        }
          
        // string
        if (type.equalsIgnoreCase(StringType.TYPE_NAME)) {
          setVariableEntity(variableName, String.valueOf(value));
          return;
        }
        
        // integer
        if (type.equalsIgnoreCase(IntegerType.TYPE_NAME)) {
          setVariableEntity(variableName, Integer.valueOf(value.toString()));
          return;
        }
        
        // short
        if (type.equalsIgnoreCase(ShortType.TYPE_NAME)) {
          setVariableEntity(variableName, Short.valueOf(value.toString()));
          return;
        } 
        
        // long
        if (type.equalsIgnoreCase(LongType.TYPE_NAME)) {
          setVariableEntity(variableName, Long.valueOf(value.toString()));
          return;
        }
  
        // double
        if (type.equalsIgnoreCase(DoubleType.TYPE_NAME)) {
          setVariableEntity(variableName, Double.valueOf(value.toString()));
          return;
        }
        
        // date
        if (type.equalsIgnoreCase(DateType.TYPE_NAME)) {
          SimpleDateFormat pattern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
          Date date = pattern.parse(String.valueOf(value));
          setVariableEntity(variableName, date);
          return;
        }
        
        // passed a non supported type
        throw new IllegalArgumentException("The variable type '" + type + "' is not supported.");
      }
      
      // no type specified or value equals null then simply set the variable
      setVariableEntity(variableName, variable.getValue());
      
    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot put %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
      
    } catch (NumberFormatException e) {
      String errorMessage = String.format("Cannot put %s variable %s due to number format exception: %s", getResourceTypeName(), variableName, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);
      
    } catch (ParseException e) {
      String errorMessage = String.format("Cannot put %s variable %s due to parse exception: %s", getResourceTypeName(), variableName, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);      
    
    } catch (IllegalArgumentException e) {
      String errorMessage = String.format("Cannot put %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, errorMessage);  
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
    Map<String, Object> variableModifications = null;
    try {
      variableModifications = DtoUtil.toMap(patch.getModifications());
      
    } catch (NumberFormatException e) {
      String errorMessage = String.format("Cannot modify variables for %s due to number format exception: %s", getResourceTypeName(), e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);
      
    } catch (ParseException e) {
      String errorMessage = String.format("Cannot modify variables for %s due to parse exception: %s", getResourceTypeName(), e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);      
    
    } catch (IllegalArgumentException e) {
      String errorMessage = String.format("Cannot modify variables for %s: %s", getResourceTypeName(), e.getMessage());
      throw new RestException(Status.BAD_REQUEST, errorMessage);  
    }
      
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
