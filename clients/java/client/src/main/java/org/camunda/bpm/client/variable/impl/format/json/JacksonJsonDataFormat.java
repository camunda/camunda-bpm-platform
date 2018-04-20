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
package org.camunda.bpm.client.variable.impl.format.json;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.client.spi.DataFormat;
import org.camunda.bpm.client.variable.impl.format.TypeDetector;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JacksonJsonDataFormat implements DataFormat {

  protected ObjectMapper objectMapper;
  protected List<TypeDetector> typeDetectors;

  public JacksonJsonDataFormat() {
    this(new ObjectMapper());
  }

  public JacksonJsonDataFormat(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.typeDetectors = new ArrayList<TypeDetector>();
    this.typeDetectors.add(new ListJacksonJsonTypeDetector());
    this.typeDetectors.add(new DefaultJsonJacksonTypeDetector());
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public boolean canMap(Object parameter) {
    if(parameter != null) {
      return objectMapper.canSerialize(parameter.getClass());
    }
    else {
      return false;
    }
  }

  public String writeValue(Object value) throws Exception {
    return objectMapper.writeValueAsString(value);
  }

  @SuppressWarnings("unchecked")
  public <T> T readValue(String value, String typeIdentifier) throws Exception {
    try {
      Class<?> cls = Class.forName(typeIdentifier);
      return (T) readValue(value, cls);
    }
    catch (ClassNotFoundException e) {
      JavaType javaType = constructJavaTypeFromCanonicalString(typeIdentifier);
      return readValue(value, javaType);
    }
  }

  public <T> T readValue(String value, Class<T> cls) throws Exception {
    return objectMapper.readValue(value, cls);
  }

  protected <C> C readValue(String value, JavaType type) throws Exception {
    return objectMapper.readValue(value, type);
  }

  public JavaType constructJavaTypeFromCanonicalString(String canonicalString) {
    try {
      return TypeFactory.defaultInstance().constructFromCanonical(canonicalString);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    }
  }

  public String getCanonicalTypeName(Object value) {
    ensureNotNull("value", value);

    for (TypeDetector typeDetector : typeDetectors) {
      if (typeDetector.canHandle(value)) {
        return typeDetector.detectType(value);
      }
    }

    // TODO
    throw new RuntimeException();
  }

}
