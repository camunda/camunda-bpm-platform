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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.dto.PatchVariablesDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData.FormPart;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractVariablesResource implements VariableResource {

  protected static final String DEFAULT_BINARY_VALUE_TYPE = "Bytes";

  protected ProcessEngine engine;
  protected String resourceId;
  protected ObjectMapper objectMapper;

  public AbstractVariablesResource(ProcessEngine engine, String resourceId, ObjectMapper objectMapper) {
    this.engine = engine;
    this.resourceId = resourceId;
    this.objectMapper = objectMapper;
  }

  @Override
  public Map<String, VariableValueDto> getVariables(boolean deserializeValues) {

    VariableMap variables = getVariableEntities(deserializeValues);

    Map<String, VariableValueDto> values = new HashMap<String, VariableValueDto>();
    for (String variableName : variables.keySet()) {
      VariableValueDto valueDto = VariableValueDto.fromTypedValue(variables.getValueTyped(variableName));
      values.put(variableName, valueDto);
    }

    return values;
  }

  @Override
  public VariableValueDto getVariable(String variableName, boolean deserializeValue) {
    TypedValue value = getTypedValueForVariable(variableName, deserializeValue);
    return VariableValueDto.fromTypedValue(value);

  }

  protected TypedValue getTypedValueForVariable(String variableName, boolean deserializeValue) {
    TypedValue value = null;
    try {
       value = getVariableEntity(variableName, deserializeValue);
    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot get %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }

    if (value == null) {
      String errorMessage = String.format("%s variable with name %s does not exist", getResourceTypeName(), variableName);
      throw new InvalidRequestException(Status.NOT_FOUND, errorMessage);
    }
    return value;
  }

  public Response getVariableBinary(String variableName) {
    TypedValue typedValue = getTypedValueForVariable(variableName, false);
    return new VariableResponseProvider().getResponseForTypedVariable(typedValue, resourceId);
  }

  @Override
  public void putVariable(String variableName, VariableValueDto variable) {

    try {
      TypedValue typedValue = variable.toTypedValue(engine, objectMapper);
      setVariableEntity(variableName, typedValue);

    } catch (RestException e) {
      throw new InvalidRequestException(e.getStatus(), e,
        String.format("Cannot put %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage()));
    } catch (BadUserRequestException e) {
      throw new RestException(Status.BAD_REQUEST, e,
        String.format("Cannot put %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage()));
    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e,
          String.format("Cannot put %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage()));
    }
  }

  public void setBinaryVariable(String variableKey, MultipartFormData payload) {
    FormPart dataPart = payload.getNamedPart("data");
    FormPart objectTypePart = payload.getNamedPart("type");
    FormPart valueTypePart = payload.getNamedPart("valueType");

    if(objectTypePart != null) {
      Object object = null;

      if(dataPart.getContentType()!=null
          && dataPart.getContentType().toLowerCase().contains(MediaType.APPLICATION_JSON)) {

        object = deserializeJsonObject(objectTypePart.getTextContent(), dataPart.getBinaryContent());

      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Unrecognized content type for serialized java type: "+dataPart.getContentType());
      }

      if(object != null) {
        setVariableEntity(variableKey, Variables.objectValue(object).create());
      }
    } else {

      String valueTypeName = DEFAULT_BINARY_VALUE_TYPE;
      if (valueTypePart != null) {
        if (valueTypePart.getTextContent() == null) {
          throw new InvalidRequestException(Status.BAD_REQUEST,
              "Form part with name 'valueType' must have a text/plain value");
        }

        valueTypeName = valueTypePart.getTextContent();
      }

      VariableValueDto valueDto = VariableValueDto.fromFormPart(valueTypeName, dataPart);
      try {

        TypedValue typedValue = valueDto.toTypedValue(engine, objectMapper);
        setVariableEntity(variableKey, typedValue);
      } catch (AuthorizationException e) {
        throw e;
      } catch (ProcessEngineException e) {
        String errorMessage = String.format("Cannot put %s variable %s: %s", getResourceTypeName(), variableKey, e.getMessage());
        throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
      }
    }
  }

  protected Object deserializeJsonObject(String className, byte[] data) {
    try {
      JavaType type = TypeFactory.defaultInstance().constructFromCanonical(className);

      return objectMapper.readValue(new String(data, Charset.forName("UTF-8")), type);

    } catch(Exception e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Could not deserialize JSON object: "+e.getMessage());

    }
  }

  @Override
  public void deleteVariable(String variableName) {
    try {
      removeVariableEntity(variableName);
    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot delete %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }

  }

  @Override
  public void modifyVariables(PatchVariablesDto patch) {
    VariableMap variableModifications = null;
    try {
      variableModifications = VariableValueDto.toMap(patch.getModifications(), engine, objectMapper);

    } catch (RestException e) {
      String errorMessage = String.format("Cannot modify variables for %s: %s", getResourceTypeName(), e.getMessage());
      throw new InvalidRequestException(e.getStatus(), e, errorMessage);

    }

    List<String> variableDeletions = patch.getDeletions();

    try {
      updateVariableEntities(variableModifications, variableDeletions);
    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot modify variables for %s %s: %s", getResourceTypeName(), resourceId, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }


  }

  protected abstract VariableMap getVariableEntities(boolean deserializeValues);

  protected abstract void updateVariableEntities(VariableMap variables, List<String> deletions);

  protected abstract TypedValue getVariableEntity(String variableKey, boolean deserializeValue);

  protected abstract void setVariableEntity(String variableKey, TypedValue variableValue);

  protected abstract void removeVariableEntity(String variableKey);

  protected abstract String getResourceTypeName();

}
