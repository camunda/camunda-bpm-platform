package org.camunda.bpm.engine.rest.application;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * a provider that is in conflict with the jackson provider.
 * 
 * The produced content type has to be lexicographically sortable before 'application/json' to ensure it is picked instead of jackson
 * by the JAX-RS runtime in case there is no content type defined in the response by the resource method.
 * @author Thorben Lindhauer
 *
 */
@Provider
@Produces("aaa/aaa")
public class ConflictingProvider implements MessageBodyWriter<Object> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return true;
  }

  @Override
  public long getSize(Object t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Object t, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    entityStream.write("Conflicting provider used".getBytes());
  }

}
