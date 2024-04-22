/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.spin.impl.json.jackson.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.camunda.spin.DeserializationTypeValidator;
import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.impl.json.jackson.JacksonJsonLogger;
import org.camunda.spin.spi.DataFormatMapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JacksonJsonDataFormatMapper implements DataFormatMapper {

  private static final JacksonJsonLogger LOG = JacksonJsonLogger.JSON_TREE_LOGGER;

  protected JacksonJsonDataFormat format;

  public JacksonJsonDataFormatMapper(JacksonJsonDataFormat format) {
    this.format = format;
  }

  public boolean canMap(Object parameter) {
    // Jackson ObjectMapper#canSerialize() method was removed
    // due to causing performance issues in high load scenarios
    return parameter != null;
  }

  public String getCanonicalTypeName(Object object) {
    return format.getCanonicalTypeName(object);
  }

  public Object mapJavaToInternal(Object parameter) {
    ObjectMapper mapper = format.getObjectMapper();
    try {
      return mapper.valueToTree(parameter);
    } catch (IllegalArgumentException e) {
      throw LOG.unableToMapInput(parameter, e);
    }
  }

  public <T> T mapInternalToJava(Object parameter, Class<T> type) {
    return mapInternalToJava(parameter, type, null);
  }

  @Override
  public <T> T mapInternalToJava(Object parameter, Class<T> type, DeserializationTypeValidator validator) {
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    return mapInternalToJava(parameter, javaType, validator);
  }

  public <T> T mapInternalToJava(Object parameter, String typeIdentifier) {
    return mapInternalToJava(parameter, typeIdentifier, null);
  }

  @Override
  public <T> T mapInternalToJava(Object parameter, String typeIdentifier, DeserializationTypeValidator validator) {
    try {
      //sometimes the class identifier is at once a fully qualified class name
      final Class<?> aClass = Class.forName(typeIdentifier, true, Thread.currentThread().getContextClassLoader());
      return (T) mapInternalToJava(parameter, aClass, validator);
    } catch (ClassNotFoundException e) {
      JavaType javaType = format.constructJavaTypeFromCanonicalString(typeIdentifier);
      T result = mapInternalToJava(parameter, javaType, validator);
      return result;
    }
  }

  public <C> C mapInternalToJava(Object parameter, JavaType type) {
    return mapInternalToJava(parameter, type, null);
  }

  public <C> C mapInternalToJava(Object parameter, JavaType type, DeserializationTypeValidator validator) {
    JsonNode jsonNode = (JsonNode) parameter;
    try {
      validateType(type, validator);
      ObjectMapper mapper = format.getObjectMapper();
      return mapper.readValue(mapper.treeAsTokens(jsonNode), type);
    } catch (IOException | SpinRuntimeException e) {
      throw LOG.unableToDeserialize(jsonNode, type, e);
    }
  }

  /**
   * Validate the type with the help of the validator.<br>
   * Note: when adjusting this method, please also consider adjusting
   * the {@code AbstractVariablesResource#validateType} in the REST API
   */
  protected void validateType(JavaType type, DeserializationTypeValidator validator) {
    if (validator != null) {
      List<String> invalidTypes = new ArrayList<>();
      validateType(type, validator, invalidTypes);
      if (!invalidTypes.isEmpty()) {
        throw new SpinRuntimeException("The following classes are not whitelisted for deserialization: " + invalidTypes);
      }
    }
  }

  protected void validateType(JavaType type, DeserializationTypeValidator validator, List<String> invalidTypes) {
    if (!type.isPrimitive()) {
      if (!type.isArrayType()) {
        validateTypeInternal(type, validator, invalidTypes);
      }
      if (type.isMapLikeType()) {
        validateType(type.getKeyType(), validator, invalidTypes);
      }
      if (type.isContainerType() || type.hasContentType()) {
        validateType(type.getContentType(), validator, invalidTypes);
      }
    }
  }

  protected void validateTypeInternal(JavaType type, DeserializationTypeValidator validator, List<String> invalidTypes) {
    String className = type.getRawClass().getName();
    if (!validator.validate(className) && !invalidTypes.contains(className)) {
      invalidTypes.add(className);
    }
  }

}
