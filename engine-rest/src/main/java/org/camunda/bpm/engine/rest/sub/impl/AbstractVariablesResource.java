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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

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
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;


public abstract class AbstractVariablesResource implements VariableResource {

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

  public InputStream getVariableBinary(String variableName) {
    TypedValue typedValue = getTypedValueForVariable(variableName, false);
    if(typedValue instanceof BytesValue) {
      byte[] valueBytes = ((BytesValue)typedValue).getValue();
      if (valueBytes == null) {
        valueBytes = new byte[0];
      }

      return new ByteArrayInputStream(valueBytes);
    }
    else {
      throw new InvalidRequestException(Status.BAD_REQUEST, "Variable '"+variableName+"' is not of type 'Bytes' but of type '"+typedValue.getType()+"'.");
    }
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

    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e,
          String.format("Cannot put %s variable %s: %s", getResourceTypeName(), variableName, e.getMessage()));
    }
  }

  public void setBinaryVariable(String variableKey, MultipartFormData payload) {
    FormPart dataPart = payload.getNamedPart("data");
    FormPart valueTypePart = payload.getNamedPart("type");

    if(valueTypePart != null) {
      Object object = null;

      if(dataPart.getContentType()!=null
          && dataPart.getContentType().toLowerCase().contains(MediaType.APPLICATION_JSON)) {

        object = deserializeJsonObject(valueTypePart.getTextContent(), dataPart.getBinaryContent());

      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Unrecognized content type for serialized java type: "+dataPart.getContentType());
      }

      if(object != null) {
        setVariableEntity(variableKey, Variables.objectValue(object).create());
      }
    } else {
      try {
        setVariableEntity(variableKey, Variables.byteArrayValue(dataPart.getBinaryContent()));
      } catch (ProcessEngineException e) {
        String errorMessage = String.format("Cannot put %s variable %s: %s", getResourceTypeName(), variableKey, e.getMessage());
        throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
      }
    }
  }

  protected Object deserializeJsonObject(String className, byte[] data) {
    try {
      JavaType type = TypeFactory.fromCanonical(className);

      return objectMapper.readValue(new String(data, Charset.forName("UTF-8")), type);

    } catch(Exception e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, "Could not deserialize JSON object: "+e.getMessage());

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
