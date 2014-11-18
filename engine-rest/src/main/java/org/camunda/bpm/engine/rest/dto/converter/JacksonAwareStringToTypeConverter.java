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
package org.camunda.bpm.engine.rest.dto.converter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;

/**
 * @author Thorben Lindhauer
 */
public abstract class JacksonAwareStringToTypeConverter<T> implements StringToTypeConverter<T> {

  protected ObjectMapper objectMapper;

  public abstract T convertQueryParameterToType(String value);

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  protected T mapToType(String value, Class<T> typeClass) {
    try {
      return objectMapper.readValue(value, typeClass);
    } catch (JsonParseException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, String.format("Cannot convert value %s to java type %s",
          value, typeClass.getName()));
    } catch (JsonMappingException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, String.format("Cannot convert value %s to java type %s",
          value, typeClass.getName()));
    } catch (IOException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, String.format("Cannot convert value %s to java type %s",
          value, typeClass.getName()));
    }
  }
}
