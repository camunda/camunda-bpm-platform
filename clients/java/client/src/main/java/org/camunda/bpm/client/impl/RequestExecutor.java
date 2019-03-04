/*
 * Copyright © 2018-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.client.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
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
import org.camunda.commons.utils.IoUtil;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tassilo Weidner
 */
public class RequestExecutor {

  protected static final EngineClientLogger LOG = ExternalTaskClientLogger.ENGINE_CLIENT_LOGGER;

  protected static final Header HEADER_CONTENT_TYPE_JSON = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  protected static final Header HEADER_USER_AGENT = new BasicHeader(HttpHeaders.USER_AGENT, "Camunda External Task Client");

  protected HttpClient httpClient;
  protected ObjectMapper objectMapper;

  protected RequestExecutor(RequestInterceptorHandler requestInterceptorHandler, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;

    initHttpClient(requestInterceptorHandler);
  }

  protected <T> T postRequest(String resourceUrl, RequestDto requestDto, Class<T> responseClass) throws EngineClientException {
    ByteArrayEntity serializedRequest = serializeRequest(requestDto);
    HttpUriRequest httpRequest = RequestBuilder.post(resourceUrl)
      .addHeader(HEADER_USER_AGENT)
      .addHeader(HEADER_CONTENT_TYPE_JSON)
      .setEntity(serializedRequest)
      .build();
    
    return executeRequest(httpRequest, responseClass);
  }

  protected byte[] getRequest(String resourceUrl) throws EngineClientException {
    HttpUriRequest httpRequest = RequestBuilder.get(resourceUrl)
      .addHeader(HEADER_USER_AGENT)
      .addHeader(HEADER_CONTENT_TYPE_JSON)
      .build();
    
    return executeRequest(httpRequest, byte[].class);
  }

  protected <T> T executeRequest(HttpUriRequest httpRequest, Class<T> responseClass) throws EngineClientException {
    try {
      return httpClient.execute(httpRequest, handleResponse(responseClass));
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

  protected <T> ResponseHandler<T> handleResponse(final Class<T> responseClass) {
    return new AbstractResponseHandler<T>() {
      @SuppressWarnings("unchecked")
      public T handleEntity(HttpEntity responseEntity) throws IOException {
        T response = null;
        if (responseClass.isAssignableFrom(byte[].class)) {
          InputStream inputStream = null;

          try {
            inputStream = responseEntity.getContent();
            response = (T) IoUtil.inputStreamAsByteArray(inputStream);
          } finally {
            IoUtil.closeSilently(inputStream);
          }
        } else if (!responseClass.isAssignableFrom(Void.class)) {
          try {
            response = deserializeResponse(responseEntity, responseClass);
          } catch (EngineClientException e) {
            throw new RuntimeException(e);
          }
        }

        try {
          EntityUtils.consume(responseEntity);
        }
        catch (IOException e) {
          LOG.exceptionWhileClosingResourceStream(response, e);
        }

        return response;
      }

      @Override
      public T handleResponse(HttpResponse response) throws HttpResponseException, IOException {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
          InputStream inputStream = null;
          try {
            inputStream = entity.getContent();
            String error = IoUtil.inputStreamAsString(inputStream);
            throw new HttpResponseException(statusLine.getStatusCode(), error);
          } finally {
            EntityUtils.consume(entity);
            IoUtil.closeSilently(inputStream);
          }
        }
        return entity == null ? null : handleEntity(entity);
      }
    };
  }

  protected <T> T deserializeResponse(HttpEntity httpEntity, Class<T> responseClass) throws EngineClientException {
    try {
      InputStream responseBody = httpEntity.getContent();
      return objectMapper.readValue(responseBody, responseClass);
    } catch (JsonParseException e) {
      throw LOG.exceptionWhileParsingJsonObject(responseClass, e);
    } catch (JsonMappingException e) {
      throw LOG.exceptionWhileMappingJsonObject(responseClass, e);
    } catch (IOException e) {
      throw LOG.exceptionWhileDeserializingJsonObject(responseClass, e);
    }
  }

  protected ByteArrayEntity serializeRequest(RequestDto dto) throws EngineClientException {
    byte[] serializedRequest = null;

    try {
      serializedRequest = objectMapper.writeValueAsBytes(dto);
    } catch (JsonProcessingException e) {
      throw LOG.exceptionWhileSerializingJsonObject(dto, e);
    }

    ByteArrayEntity byteArrayEntity = null;
    if (serializedRequest != null) {
      byteArrayEntity = new ByteArrayEntity(serializedRequest);
    }

    return byteArrayEntity;
  }

  protected void initHttpClient(RequestInterceptorHandler requestInterceptorHandler) {
    HttpClientBuilder httpClientBuilder = HttpClients.custom()
      .useSystemProperties()
      .addInterceptorLast(requestInterceptorHandler);

    this.httpClient = httpClientBuilder.build();
  }

}
