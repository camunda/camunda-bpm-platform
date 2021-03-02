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
package org.camunda.bpm.client.variable.impl.format.json;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.spi.DataFormat;
import org.camunda.bpm.client.variable.impl.format.TypeDetector;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JacksonJsonDataFormat implements DataFormat {

  private static final JacksonJsonLogger LOG = ExternalTaskClientLogger.JSON_FORMAT_LOGGER;

  protected String name;
  protected ObjectMapper objectMapper;
  protected List<TypeDetector> typeDetectors;

  public JacksonJsonDataFormat(String name) {
    this(name, new ObjectMapper());
  }

  public JacksonJsonDataFormat(String name, ObjectMapper objectMapper) {
    this.name = name;
    this.objectMapper = objectMapper;
    this.typeDetectors = new ArrayList<>();
    this.typeDetectors.add(new ListJacksonJsonTypeDetector());
    this.typeDetectors.add(new DefaultJsonJacksonTypeDetector());
  }

  public String getName() {
    return name;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public boolean canMap(Object parameter) {
    return parameter != null;
  }

  public String writeValue(Object value) {
    try {
      StringWriter stringWriter = new StringWriter();
      objectMapper.writeValue(stringWriter, value);
      return stringWriter.toString();
    }
    catch (IOException e) {
      throw LOG.unableToWriteValue(value, e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T readValue(String value, String typeIdentifier) {
    try {
      Class<?> cls = Class.forName(typeIdentifier);
      return (T) readValue(value, cls);
    }
    catch (ClassNotFoundException e) {
      JavaType javaType = constructJavaTypeFromCanonicalString(typeIdentifier);
      return readValue(value, javaType);
    }
  }

  public <T> T readValue(String value, Class<T> cls) {
    try {
      return objectMapper.readValue(value, cls);
    }
    catch (JsonParseException e) {
      throw LOG.unableToReadValue(value, e);
    }
    catch (JsonMappingException e) {
      throw LOG.unableToReadValue(value, e);
    }
    catch (IOException e) {
      throw LOG.unableToReadValue(value, e);
    }
  }

  protected <C> C readValue(String value, JavaType type) {
    try {
      return objectMapper.readValue(value, type);
    }
    catch (JsonParseException e) {
      throw LOG.unableToReadValue(value, e);
    }
    catch (JsonMappingException e) {
      throw LOG.unableToReadValue(value, e);
    }
    catch (IOException e) {
      throw LOG.unableToReadValue(value, e);
    }
  }

  public JavaType constructJavaTypeFromCanonicalString(String canonicalString) {
    try {
      return TypeFactory.defaultInstance().constructFromCanonical(canonicalString);
    }
    catch (IllegalArgumentException e) {
      throw LOG.unableToConstructJavaType(canonicalString, e);
    }
  }

  public String getCanonicalTypeName(Object value) {
    ensureNotNull("value", value);

    for (TypeDetector typeDetector : typeDetectors) {
      if (typeDetector.canHandle(value)) {
        return typeDetector.detectType(value);
      }
    }

    throw LOG.unableToDetectCanonicalType(value);
  }

}
