package org.camunda.bpm.engine.rest.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.activiti.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.rest.dto.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.dto.SortableParameterizedQueryDto;
import org.camunda.bpm.engine.rest.dto.TaskQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;

/**
 * Reads {@link SortableParameterizedQueryDto} objects from json message bodies, supplied in POST requests.
 * @author Thorben Lindhauer
 *
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class EngineQueryDtoPostReader implements
    MessageBodyReader<SortableParameterizedQueryDto> {

  private static final String ACCEPTED_CHARSET = "UTF-8";
  
  @Context
  private Request request;

  @Override
  public boolean isReadable(Class<?> clazz, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    if (!request.getMethod().equals(HttpMethod.POST)) {
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

    String content = streamToString(entityStream);
    entityStream.close();
    JSONObject json = new JSONObject(content);
    
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
    
    Iterator<String> it = json.keys();
    while (it.hasNext()) {
      String key = it.next();
      String value = json.getString(key);
      
      try {
        queryDto.setJSONValueBasedOnAnnotation(key, value);
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
  
  private String streamToString(InputStream stream) {
    String result = "";
    Scanner s = new Scanner(stream, ACCEPTED_CHARSET).useDelimiter("\\A");
    if (s.hasNext()) {
      result = s.next();
    }
    return result;
  }

}
