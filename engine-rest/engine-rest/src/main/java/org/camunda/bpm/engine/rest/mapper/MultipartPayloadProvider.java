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
package org.camunda.bpm.engine.rest.mapper;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.RequestContext;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.mapper.MultipartFormData.FormPart;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
* <p>Provides a {@link MessageBodyReader} for {@link MultipartFormData}. This allows writing resources which
* consume {@link MediaType#MULTIPART_FORM_DATA} which is parsed into a {@link MultipartFormData} object:</p>
*
* <pre>
* {@literal @}POST
* {@literal @}Consumes(MediaType.MULTIPART_FORM_DATA)
* void handleMultipartPost(MultipartFormData multipartFormData);
* </pre>
*
* <p>The implementation used apache commons fileupload in order to parse the request and populate an instance of
* {@link MultipartFormData}.</p>
*
* @author Daniel Meyer
*/
@Provider
@Consumes(MediaType.MULTIPART_FORM_DATA)
public class MultipartPayloadProvider implements MessageBodyReader<MultipartFormData> {

  public static final String TYPE_NAME = "multipart";
  public static final String SUB_TYPE_NAME = "form-data";

  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return TYPE_NAME.equals(mediaType.getType().toLowerCase())
        && SUB_TYPE_NAME.equals(mediaType.getSubtype().toLowerCase());
  }

  public MultipartFormData readFrom(Class<MultipartFormData> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

    final MultipartFormData multipartFormData = createMultipartFormDataInstance();
    final FileUpload fileUpload = createFileUploadInstance();

    String contentType = httpHeaders.getFirst("content-type");
    RestMultipartRequestContext requestContext = createRequestContext(entityStream, contentType);

    // parse the request (populates the multipartFormData)
    parseRequest(multipartFormData, fileUpload, requestContext);

    return multipartFormData;

  }

  protected FileUpload createFileUploadInstance() {
    return new FileUpload();
  }

  protected MultipartFormData createMultipartFormDataInstance() {
    return new MultipartFormData();
  }

  protected void parseRequest(MultipartFormData multipartFormData, FileUpload fileUpload, RestMultipartRequestContext requestContext) {
    try {
      FileItemIterator itemIterator = fileUpload.getItemIterator(requestContext);
      while (itemIterator.hasNext()) {
        FileItemStream stream = itemIterator.next();
        multipartFormData.addPart(new FormPart(stream));
      }
    } catch (Exception e) {
      throw new RestException(Status.BAD_REQUEST, e, "multipart/form-data cannot be processed");

    }
  }

  protected RestMultipartRequestContext createRequestContext(InputStream entityStream, String contentType) {
    return new RestMultipartRequestContext(entityStream, contentType);
  }

  /**
   * Exposes the REST request to commons fileupload
   *
   */
  static class RestMultipartRequestContext implements RequestContext {

    protected InputStream inputStream;
    protected String contentType;

    public RestMultipartRequestContext(InputStream inputStream, String contentType) {
      this.inputStream = inputStream;
      this.contentType = contentType;
    }

    public String getCharacterEncoding() {
      return null;
    }

    public String getContentType() {
      return contentType;
    }

    public int getContentLength() {
      return -1;
    }

    public InputStream getInputStream() throws IOException {
      return inputStream;
    }

  }

}
