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
package org.camunda.bpm.client.impl.engineclient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.client.impl.ClientLogger;
import org.camunda.bpm.client.impl.dto.AbstractDto;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Tassilo Weidner
 */
public class EngineInteractionManager {

  private static final EngineClientLogger LOG = ClientLogger.ENGINE_CLIENT_LOGGER;
  private static final Header HEADER_CONTENT_TYPE_JSON = new BasicHeader("Content-Type", "application/json");

  private HttpClient httpClient;
  private ObjectMapper objectMapper;

  EngineInteractionManager() {
    this.httpClient = HttpClients.createDefault();
    this.objectMapper = new ObjectMapper();
  }

  <T, D extends AbstractDto> T postRequest(String resourceUrl, D requestDto, Class<T> responseDtoClass) {
    ByteArrayEntity serializedRequest = serializeRequest(requestDto);
    HttpUriRequest httpRequest = RequestBuilder.post(resourceUrl)
      .addHeader(HEADER_CONTENT_TYPE_JSON)
      .setEntity(serializedRequest)
      .build();

    return executeRequest(httpRequest, responseDtoClass);
  }

  private <T> T executeRequest(HttpUriRequest httpRequest, Class<T> responseDtoClass) {
    try {
      return httpClient.execute(httpRequest, handleResponse(responseDtoClass));
    } catch (HttpResponseException e) { // catches >= 300 HTTP status responses
      throw LOG.exceptionWhileReceivingResponse(httpRequest, e);
    } catch (ClientProtocolException e) {
      throw LOG.exceptionWhileEstablishingConnection(httpRequest, e);
    } catch (IOException e) {
      throw LOG.exceptionWhileEstablishingConnection(httpRequest, e);
    }
  }

  private <T> ResponseHandler<T> handleResponse(final Class<T> responseDtoClass) {
    return new AbstractResponseHandler<T>() {
      @Override
      public T handleEntity(HttpEntity responseEntity) {
        T deserializedResponse = deserializeResponse(responseEntity, responseDtoClass);

        try {
          EntityUtils.consume(responseEntity);
        } catch (IOException e) {
          LOG.exceptionWhileClosingResourceStream(deserializedResponse, e); // log without rethrow exception
        }

        return deserializedResponse;
      }
    };
  }

  private <T> T deserializeResponse(HttpEntity httpEntity, Class<T> responseDtoClass) {
    try {
      InputStream responseBody = httpEntity.getContent();
      return objectMapper.readValue(responseBody, responseDtoClass);
    } catch (JsonParseException e) {
      throw LOG.exceptionWhileParsingJsonObject(responseDtoClass, e);
    } catch (JsonMappingException e) {
      throw LOG.exceptionWhileMappingJsonObject(responseDtoClass, e);
    } catch (IOException e) {
      throw LOG.exceptionWhileDeserializingJsonObject(responseDtoClass, e);
    }
  }

  private <D extends AbstractDto> ByteArrayEntity serializeRequest(D dto) {
    byte[] serializedRequest;
    try {
      serializedRequest = objectMapper.writeValueAsBytes(dto);
    } catch (JsonProcessingException e) {
      throw LOG.exceptionWhileSerializingJsonObject(dto, e);
    }

    return new ByteArrayEntity(serializedRequest);
  }

  String sanitizeUrl(String url) {
    url = url.trim();

    if (url.endsWith("/")) {
      url = url.replaceAll("/$", "");
      url = sanitizeUrl(url);
    }

    return url;
  }

}
