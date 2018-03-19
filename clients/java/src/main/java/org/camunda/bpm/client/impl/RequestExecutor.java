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
package org.camunda.bpm.client.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.client.interceptor.impl.RequestInterceptorHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Tassilo Weidner
 */
public class RequestExecutor {

  protected static final EngineClientLogger LOG = ExternalTaskClientLogger.ENGINE_CLIENT_LOGGER;

  protected static final Header HEADER_CONTENT_TYPE_JSON = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  protected static final Header HEADER_USER_AGENT = new BasicHeader(HttpHeaders.USER_AGENT, "Camunda External Task Client");

  protected HttpClient httpClient;
  protected ObjectMapper objectMapper;

  protected RequestExecutor(RequestInterceptorHandler requestInterceptorHandler) {
    initHttpClient(requestInterceptorHandler);
    initObjectMapper();
  }

  protected <T> T postRequest(String resourceUrl, RequestDto requestDto, Class<T> responseDtoClass) throws EngineClientException {
    ByteArrayEntity serializedRequest = serializeRequest(requestDto);
    HttpUriRequest httpRequest = RequestBuilder.post(resourceUrl)
      .addHeader(HEADER_USER_AGENT)
      .addHeader(HEADER_CONTENT_TYPE_JSON)
      .setEntity(serializedRequest)
      .build();
    
    return executeRequest(httpRequest, responseDtoClass);
  }

  protected <T> T executeRequest(HttpUriRequest httpRequest, Class<T> responseDtoClass) throws EngineClientException {
    try {
      return httpClient.execute(httpRequest, handleResponse(responseDtoClass));
    } catch (RuntimeException e) {
      Throwable cause = e.getCause();
      if (cause instanceof EngineClientException) {
        throw (EngineClientException) e.getCause();
      } else {
        throw e;
      }
    } catch (HttpResponseException e) { // catches >= 300 HTTP status responses
      throw LOG.exceptionWhileReceivingResponse(httpRequest, e);
    } catch (ClientProtocolException e) {
      throw LOG.exceptionWhileEstablishingConnection(httpRequest, e);
    } catch (IOException e) {
      throw LOG.exceptionWhileEstablishingConnection(httpRequest, e);
    }
  }

  protected <T> ResponseHandler<T> handleResponse(final Class<T> responseDtoClass) {
    return new AbstractResponseHandler<T>() {
      @Override
      public T handleEntity(HttpEntity responseEntity) {
        T deserializedResponse = null;
        if (!responseDtoClass.isAssignableFrom(Void.class)) {
          try {
            deserializedResponse = deserializeResponse(responseEntity, responseDtoClass);
          } catch (EngineClientException e) {
            throw new RuntimeException(e);
          }
        }

        try {
          EntityUtils.consume(responseEntity);
        } catch (IOException e) {
          LOG.exceptionWhileClosingResourceStream(deserializedResponse, e); // log without rethrow exception
        }

        return deserializedResponse;
      }
    };
  }

  protected <T> T deserializeResponse(HttpEntity httpEntity, Class<T> responseDtoClass) throws EngineClientException {
    try {
      InputStream responseBody = httpEntity.getContent();
      return objectMapper.readValue(responseBody, responseDtoClass);
    } catch (JsonParseException e) {
      throw LOG.exceptionWhileParsingJsonObject(responseDtoClass);
    } catch (JsonMappingException e) {
      throw LOG.exceptionWhileMappingJsonObject(responseDtoClass);
    } catch (IOException e) {
      throw LOG.exceptionWhileDeserializingJsonObject(responseDtoClass);
    }
  }

  protected ByteArrayEntity serializeRequest(RequestDto dto) throws EngineClientException {
    byte[] serializedRequest = null;

    try {
      serializedRequest = objectMapper.writeValueAsBytes(dto);
    } catch (JsonProcessingException e) {
      throw LOG.exceptionWhileSerializingJsonObject(dto);
    }

    ByteArrayEntity byteArrayEntity = null;
    if (serializedRequest != null) {
      byteArrayEntity = new ByteArrayEntity(serializedRequest);
    }

    return byteArrayEntity;
  }

  protected String sanitizeUrl(String url) {
    url = url.trim();

    if (url.endsWith("/")) {
      url = url.replaceAll("/$", "");
      url = sanitizeUrl(url);
    }

    return url;
  }

  protected void initHttpClient(RequestInterceptorHandler requestInterceptorHandler) {
    HttpClientBuilder httpClientBuilder = HttpClients.custom()
      .addInterceptorLast(requestInterceptorHandler);

    this.httpClient = httpClientBuilder.build();
  }

  protected void initObjectMapper() {
    this.objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
  }

}
