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
package org.camunda.bpm.engine.rest.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.SortableParameterizedQueryDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;

/**
 * A {@link MessageBodyReader} that populates subclasses of
 * {@link SortableParameterizedQueryDto} from http query parameters. Parameters are
 * matched to setter methods in the class that are annotated with
 * {@link CamundaQueryParam}.
 * 
 * @author Thorben Lindhauer
 * 
 */
@Provider
public class EngineQueryDtoGetReader implements
    MessageBodyReader<SortableParameterizedQueryDto> {

  @Context
  private UriInfo context;
  
  @Context
  private Request request;

  @Override
  public boolean isReadable(Class<?> clazz, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    if (!request.getMethod().equals(HttpMethod.GET)) {
      return false;
    }
    
    if (clazz == ProcessDefinitionQueryDto.class) {
      return true;
    }
    if (clazz == ProcessInstanceQueryDto.class) {
      return true;
    }
    if (clazz == TaskQueryDto.class) {
      return true;
    }
    return false;
  }

  @Override
  public SortableParameterizedQueryDto readFrom(
      Class<SortableParameterizedQueryDto> clazz, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {

    MultivaluedMap<String, String> queryParameters = context
        .getQueryParameters();
    SortableParameterizedQueryDto queryDto;
    try {
      queryDto = clazz.newInstance();
    } catch (InstantiationException e) {
      throw new WebApplicationException(
          Status.INTERNAL_SERVER_ERROR.getStatusCode());
    } catch (IllegalAccessException e) {
      throw new WebApplicationException(
          Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    for (Entry<String, List<String>> param : queryParameters.entrySet()) {
      String key = param.getKey();
      String value = param.getValue().iterator().next();

      try {
        queryDto.setValueBasedOnAnnotation(key, value);
      } catch (IllegalArgumentException e) {
        throw new InvalidRequestException("Cannot set parameter.");
      } catch (IllegalAccessException e) {
        throw new RestException("Server error.");
      } catch (InvocationTargetException e) {
        throw new InvalidRequestException("Cannot set parameter.");
      } catch (InstantiationException e) {
        throw new RestException("Server error.");
      }
    }

    return queryDto;
  }

}
