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
package org.camunda.bpm.engine.rest.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData.FormPart;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.type.AbstractValueTypeImpl;
import org.camunda.bpm.engine.variable.type.FileValueType;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.type.ValueTypeResolver;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 *
 */
public class VariableValueDto {

  protected String type;
  protected Object value;
  protected Map<String, Object> valueInfo;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Map<String, Object> getValueInfo() {
    return valueInfo;
  }

  public void setValueInfo(Map<String, Object> valueInfo) {
    this.valueInfo = valueInfo;
  }

  public TypedValue toTypedValue(ProcessEngine processEngine, ObjectMapper objectMapper) {
    ValueTypeResolver valueTypeResolver = processEngine.getProcessEngineConfiguration().getValueTypeResolver();

    if (type == null) {
      if (valueInfo != null && valueInfo.get(ValueType.VALUE_INFO_TRANSIENT) instanceof Boolean) {
        return Variables.untypedValue(value, (Boolean) valueInfo.get(ValueType.VALUE_INFO_TRANSIENT));
      }
      return Variables.untypedValue(value);
    }

    ValueType valueType = valueTypeResolver.typeForName(fromRestApiTypeName(type));
    if(valueType == null) {
      throw new RestException(Status.BAD_REQUEST, String.format("Unsupported value type '%s'", type));
    }
    else {
      if(valueType instanceof PrimitiveValueType) {
        PrimitiveValueType primitiveValueType = (PrimitiveValueType) valueType;
        Class<?> javaType = primitiveValueType.getJavaType();
        Object mappedValue = null;
        try {
          if(value != null) {
            if(javaType.isAssignableFrom(value.getClass())) {
              mappedValue = value;
            }
            else {
              // use jackson to map the value to the requested java type
              mappedValue = objectMapper.readValue("\""+value+"\"", javaType);
            }
          }
          return valueType.createValue(mappedValue, valueInfo);
        }
        catch (Exception e) {
          throw new InvalidRequestException(Status.BAD_REQUEST, e,
              String.format("Cannot convert value '%s' of type '%s' to java type %s", value, type, javaType.getName()));
        }
      }
      else if(valueType instanceof SerializableValueType) {
        if(value != null && !(value instanceof String)) {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Must provide 'null' or String value for value of SerializableValue type '"+type+"'.");
        }
        return ((SerializableValueType) valueType).createValueFromSerialized((String) value, valueInfo);
      }
      else if(valueType instanceof FileValueType) {

        if (value instanceof String) {
          value = Base64.decodeBase64((String) value);
        }

        return valueType.createValue(value, valueInfo);
      } else {
        return valueType.createValue(value, valueInfo);
      }
    }

  }

  protected FileValue fileValueWithDecodedString(FileValue fileValue, String value) {
    return Variables.fileValue(fileValue.getFilename())
                    .file(Base64.decodeBase64(value))
                    .mimeType(fileValue.getMimeType())
                    .encoding(fileValue.getEncoding())
                    .create();
  }

  public static VariableMap toMap(Map<String, VariableValueDto> variables, ProcessEngine processEngine, ObjectMapper objectMapper) {
    if(variables == null) {
      return null;
    }

    VariableMap result = Variables.createVariables();
    for (Entry<String, VariableValueDto> variableEntry : variables.entrySet()) {
      result.put(variableEntry.getKey(), variableEntry.getValue().toTypedValue(processEngine, objectMapper));
    }

    return result;
  }

  public static VariableValueDto fromTypedValue(TypedValue typedValue) {
    VariableValueDto dto = new VariableValueDto();
    fromTypedValue(dto, typedValue);
    return dto;
  }

  public static VariableValueDto fromTypedValue(TypedValue typedValue, boolean preferSerializedValue) {
    VariableValueDto dto = new VariableValueDto();
    fromTypedValue(dto, typedValue, preferSerializedValue);
    return dto;
  }

  public static void fromTypedValue(VariableValueDto dto, TypedValue typedValue) {
    fromTypedValue(dto, typedValue, false);
  }

  public static void fromTypedValue(VariableValueDto dto, TypedValue typedValue, boolean preferSerializedValue) {

    ValueType type = typedValue.getType();
    if (type != null) {
      String typeName = type.getName();
      dto.setType(toRestApiTypeName(typeName));
      dto.setValueInfo(type.getValueInfo(typedValue));
    }

    if(typedValue instanceof SerializableValue) {
      SerializableValue serializableValue = (SerializableValue) typedValue;

      if(serializableValue.isDeserialized() && !preferSerializedValue) {
        dto.setValue(serializableValue.getValue());
      }
      else {
        dto.setValue(serializableValue.getValueSerialized());
      }

    }
    else if(typedValue instanceof FileValue){
      //do not set the value for FileValues since we don't want to send megabytes over the network without explicit request
    }
    else {
      dto.setValue(typedValue.getValue());
    }

  }

  public static String toRestApiTypeName(String name) {
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  public static String fromRestApiTypeName(String name) {
    return name.substring(0, 1).toLowerCase() + name.substring(1);
  }

  public static Map<String, VariableValueDto> fromVariableMap(VariableMap variables) {
    Map<String, VariableValueDto> result = new HashMap<String, VariableValueDto>();
    for(String name: variables.keySet()) {
      result.put(name, fromTypedValue(variables.getValueTyped(name)));
    }
    return result;
  }

  public static VariableValueDto fromFormPart(String type, FormPart binaryDataFormPart) {
    VariableValueDto dto = new VariableValueDto();

    dto.type = type;
    dto.value = binaryDataFormPart.getBinaryContent();

    if (ValueType.FILE.getName().equals(fromRestApiTypeName(type))) {

      String contentType = binaryDataFormPart.getContentType();
      if (contentType == null) {
        contentType = MediaType.APPLICATION_OCTET_STREAM;
      }

      dto.valueInfo = new HashMap<String, Object>();
      dto.valueInfo.put(FileValueType.VALUE_INFO_FILE_NAME, binaryDataFormPart.getFileName());
      MimeType mimeType = null;
      try {
        mimeType = new MimeType(contentType);
      } catch (MimeTypeParseException e) {
        throw new RestException(Status.BAD_REQUEST, "Invalid mime type given");
      }

      dto.valueInfo.put(FileValueType.VALUE_INFO_FILE_MIME_TYPE, mimeType.getBaseType());

      String encoding = mimeType.getParameter("encoding");
      if (encoding != null) {
        dto.valueInfo.put(FileValueType.VALUE_INFO_FILE_ENCODING, encoding);
      }

      String transientString = mimeType.getParameter("transient");
      boolean isTransient = Boolean.parseBoolean(transientString);
      if (isTransient) {
        dto.valueInfo.put(AbstractValueTypeImpl.VALUE_INFO_TRANSIENT, isTransient);
      }
    }

    return dto;


  }

}
