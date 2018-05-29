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
package org.camunda.spin.impl.json.jackson.format;

import java.io.IOException;

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
    JavaType javaType = TypeFactory.defaultInstance().constructType(type);
    T result = mapInternalToJava(parameter, javaType);
    return result;
  }

  public <T> T mapInternalToJava(Object parameter, String typeIdentifier) {
    try {
      //sometimes the class identifier is at once a fully qualified class name
      final Class<?> aClass = Class.forName(typeIdentifier);
      return (T) mapInternalToJava(parameter, aClass);
    } catch (ClassNotFoundException e) {
      JavaType javaType = format.constructJavaTypeFromCanonicalString(typeIdentifier);
      T result = mapInternalToJava(parameter, javaType);
      return result;
    }
  }

  public <C> C mapInternalToJava(Object parameter, JavaType type) {
    JsonNode jsonNode = (JsonNode) parameter;
    ObjectMapper mapper = format.getObjectMapper();
    try {
      return mapper.readValue(mapper.treeAsTokens(jsonNode), type);
    } catch (IOException e) {
      throw LOG.unableToDeserialize(jsonNode, type, e);
    }
  }

}
