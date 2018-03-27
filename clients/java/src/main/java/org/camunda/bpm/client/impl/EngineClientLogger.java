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

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpResponseException;
import org.camunda.bpm.client.exception.UnsupportedTypeException;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;

import java.io.IOException;

/**
 * @author Tassilo Weidner
 */
public class EngineClientLogger extends ExternalTaskClientLogger {

  protected EngineClientException exceptionWhileReceivingResponse(HttpRequest httpRequest, HttpResponseException e) {
    return new EngineClientException(exceptionMessage(
      "001", "Request '{}' returned error: status code '{}' - message: {}",
      httpRequest, e.getStatusCode(), e.getMessage()), e);
  }

  protected EngineClientException exceptionWhileEstablishingConnection(HttpRequest httpRequest, IOException e) {
    return new EngineClientException(exceptionMessage(
      "002", "Exception while establishing connection for request '{}'", httpRequest), e);
  }

  protected <T> void exceptionWhileClosingResourceStream(T response, IOException e) {
    logError(
      "003", "Exception while closing resource stream of response '{}': {}", response, e);
  }

  protected <T> EngineClientException exceptionWhileParsingJsonObject(Class<T> responseDtoClass) {
    return new EngineClientException(exceptionMessage(
      "004", "Exception while parsing json object to response dto class '{}'", responseDtoClass));
  }

  protected <T> EngineClientException exceptionWhileMappingJsonObject(Class<T> responseDtoClass) {
    return new EngineClientException(exceptionMessage(
      "005", "Exception while mapping json object to response dto class '{}'", responseDtoClass));
  }

  protected <T> EngineClientException exceptionWhileDeserializingJsonObject(Class<T> responseDtoClass) {
    return new EngineClientException(exceptionMessage(
      "006", "Exception while deserializing json object to response dto class '{}'", responseDtoClass));
  }

  protected <D extends RequestDto> EngineClientException exceptionWhileSerializingJsonObject(D dto) {
    return new EngineClientException(exceptionMessage(
      "007", "Exception while serializing json object to '{}'", dto));
  }

  public void requestInterceptorException(Throwable e) {
    logError(
      "008", "Exception while executing request interceptor: {}", e);
  }

  public EngineClientException exceptionWhileDeserializingVariables(String variableName, String variableType) {
    return new EngineClientException(exceptionMessage(
      "009", "Exception while deserializing variable {}: no suitable mapper found for type {}", variableName, variableType));
  }

  public EngineClientException exceptionWhileDeserializingVariablesWrongType(String variableName, String variableType, Object variableValue) {
    return new EngineClientException(exceptionMessage(
      "010", "Exception while deserializing variable '{}': value '{}' does not match type '{}'",
      variableName, variableValue, variableType));
  }

  public EngineClientException unsupportedSerializationDataFormatException(String serializationDataFormat, TypedValueDto typedValueDto) {
    return new EngineClientException(exceptionMessage(
      "011", "Exception while deserializing variable: value '{}' has unsupported serialization data format '{}'",
      typedValueDto, serializationDataFormat));
  }

  public EngineClientException missingSpinXmlDependencyExceptionInternal() {
    return new EngineClientException(exceptionMessage(
      "012", "Exception while deserializing object value of type 'xml': the dependency 'camunda-spin-dataformat-xml-dom' needs to be added"));
  }

  public EngineClientException invalidSerializedValueException(String serializedValue, String message) {
    return new EngineClientException(exceptionMessage(
      "013", "Exception while deserializing object value '{}': {}", serializedValue, message));
  }

  public EngineClientException objectTypeNameUnknownException(String objectTypeName, Object serializedValue) {
    return new EngineClientException(exceptionMessage(
      "014", "Exception while deserializing object value '{}': object type '{}' is unknown", serializedValue, objectTypeName));
  }
}
