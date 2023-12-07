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

import org.apache.hc.core5.http.HttpRequest;
import org.camunda.bpm.client.exception.RestException;

import java.io.IOException;

/**
 * @author Tassilo Weidner
 */
public class EngineClientLogger extends ExternalTaskClientLogger {

  protected EngineClientException exceptionWhileReceivingResponse(HttpRequest httpRequest, RestException e) {
    return new EngineClientException(exceptionMessage(
      "001", "Request '{}' returned error: status code '{}' - message: {}", httpRequest, e.getHttpStatusCode(), e.getMessage()), e);
  }

  protected EngineClientException exceptionWhileEstablishingConnection(HttpRequest httpRequest, IOException e) {
    return new EngineClientException(exceptionMessage(
      "002", "Exception while establishing connection for request '{}'", httpRequest), e);
  }

  protected <T> void exceptionWhileClosingResourceStream(T response, IOException e) {
    logError(
      "003", "Exception while closing resource stream of response '" + response + "': ", e);
  }

  protected <T> EngineClientException exceptionWhileParsingJsonObject(Class<T> responseDtoClass, Throwable t) {
    return new EngineClientException(exceptionMessage(
      "004", "Exception while parsing json object to response dto class '{}'", responseDtoClass), t);
  }

  protected <T> EngineClientException exceptionWhileMappingJsonObject(Class<T> responseDtoClass, Throwable t) {
    return new EngineClientException(exceptionMessage(
      "005", "Exception while mapping json object to response dto class '{}'", responseDtoClass), t);
  }

  protected <T> EngineClientException exceptionWhileDeserializingJsonObject(Class<T> responseDtoClass, Throwable t) {
    return new EngineClientException(exceptionMessage(
      "006", "Exception while deserializing json object to response dto class '{}'", responseDtoClass), t);
  }

  protected <D extends RequestDto> EngineClientException exceptionWhileSerializingJsonObject(D dto, Throwable t) {
    return new EngineClientException(exceptionMessage(
      "007", "Exception while serializing json object to '{}'", dto), t);
  }

  public void requestInterceptorException(Throwable e) {
    logError(
      "008", "Exception while executing request interceptor: {}", e);
  }

}
