package org.camunda.bpm.engine.rest.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.camunda.bpm.engine.rest.dto.SortableParameterizedQueryDto;
import org.camunda.bpm.engine.rest.dto.ProcessDefinitionQueryDto;
import org.camunda.bpm.engine.rest.dto.ProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;

@Provider
public class EngineQueryDtoReader implements
    MessageBodyReader<SortableParameterizedQueryDto> {

  @Context
  private UriInfo context;

  @Override
  public boolean isReadable(Class<?> clazz, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    if (clazz == ProcessDefinitionQueryDto.class) {
      return true;
    }
    if (clazz == ProcessInstanceQueryDto.class) {
      return true;
    }
    return false;
  }

  @Override
  public SortableParameterizedQueryDto readFrom(
      Class<SortableParameterizedQueryDto> clazz, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    
    MultivaluedMap<String, String> queryParameters = context.getQueryParameters();
    SortableParameterizedQueryDto queryDto;
    try {
       queryDto = clazz.newInstance();
    } catch (InstantiationException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    } catch (IllegalAccessException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
  
     for (Entry<String, List<String>> param : queryParameters.entrySet()) {
       String key = param.getKey();
       String value = param.getValue().iterator().next();
       
       try {
         queryDto.setPropertyFromParameterPair(key, value);
       } catch (InvalidRequestException e) {
         throw new WebApplicationException(Status.BAD_REQUEST.getStatusCode());
       } catch (RestException e) {
         throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode());
       }
     }
    
    return queryDto;
  }

}
