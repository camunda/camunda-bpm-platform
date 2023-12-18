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
package org.camunda.bpm.client.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.StatusLine;
import org.camunda.bpm.client.exception.RestException;
import org.camunda.commons.utils.IoUtil;

/**
 * @author Tassilo Weidner
 */
public class RequestExecutor {

  protected static final EngineClientLogger LOG = ExternalTaskClientLogger.ENGINE_CLIENT_LOGGER;

  protected static final Header HEADER_CONTENT_TYPE_JSON = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  protected static final Header HEADER_USER_AGENT = new BasicHeader(HttpHeaders.USER_AGENT, "Camunda External Task Client");

  protected HttpClient httpClient;
  protected ObjectMapper objectMapper;

  protected RequestExecutor(HttpClient httpClient, ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
  }

  protected <T> T postRequest(String resourceUrl, RequestDto requestDto, Class<T> responseClass) {
    ByteArrayEntity serializedRequest = serializeRequest(requestDto);
    ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(URI.create(resourceUrl).normalize())
      .addHeader(HEADER_USER_AGENT)
      .addHeader(HEADER_CONTENT_TYPE_JSON)
      .setEntity(serializedRequest)
      .build();

    return executeRequest(httpRequest, responseClass);
  }

  protected byte[] getRequest(String resourceUrl)  {
    ClassicHttpRequest httpRequest = ClassicRequestBuilder.get(URI.create(resourceUrl).normalize())
      .addHeader(HEADER_USER_AGENT)
      .addHeader(HEADER_CONTENT_TYPE_JSON)
      .build();

    return executeRequest(httpRequest, byte[].class);
  }

  protected <T> T executeRequest(ClassicHttpRequest httpRequest, Class<T> responseClass) {
    try {
      return httpClient.execute(httpRequest, handleResponse(responseClass));

    } catch (RestException e) { // catches >= 300 HTTP status responses
      throw LOG.exceptionWhileReceivingResponse(httpRequest, e);

    } catch (IOException e) { // connection was aborted
      throw LOG.exceptionWhileEstablishingConnection(httpRequest, e);

    }
  }

  protected <T> HttpClientResponseHandler<T> handleResponse(final Class<T> responseClass) {
    return new AbstractHttpClientResponseHandler<>() {
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
          response = deserializeResponse(responseEntity, responseClass);
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
      public T handleResponse(ClassicHttpResponse response) throws IOException {
        final StatusLine statusLine = new StatusLine(response);
        final HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
          try {
            RestException engineException = deserializeResponse(entity, EngineRestExceptionDto.class).toRestException();

            int statusCode = statusLine.getStatusCode();
            engineException.setHttpStatusCode(statusCode);

            throw engineException;

          } finally {
            EntityUtils.consume(entity);
          }
        }
        return entity == null ? null : handleEntity(entity);
      }
    };
  }

  protected <T> T deserializeResponse(HttpEntity httpEntity, Class<T> responseClass) {
    InputStream inputStream = null;
    try {
      inputStream = httpEntity.getContent();
      return objectMapper.readValue(inputStream, responseClass);

    } catch (JsonParseException e) {
      throw LOG.exceptionWhileParsingJsonObject(responseClass, e);

    } catch (JsonMappingException e) {
      throw LOG.exceptionWhileMappingJsonObject(responseClass, e);

    } catch (IOException e) {
      throw LOG.exceptionWhileDeserializingJsonObject(responseClass, e);

    } finally {
      IoUtil.closeSilently(inputStream);

    }
  }

  protected ByteArrayEntity serializeRequest(RequestDto dto)  {
    byte[] serializedRequest;

    try {
      serializedRequest = objectMapper.writeValueAsBytes(dto);
    } catch (JsonProcessingException e) {
      throw LOG.exceptionWhileSerializingJsonObject(dto, e);
    }

    ByteArrayEntity byteArrayEntity = null;
    if (serializedRequest != null) {
      byteArrayEntity = new ByteArrayEntity(serializedRequest, ContentType.APPLICATION_JSON);
    }

    return byteArrayEntity;
  }

}
