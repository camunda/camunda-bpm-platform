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

import java.io.IOException;
import java.util.Collection;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.camunda.bpm.client.exception.ConnectionLostException;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.exception.NotAcquiredException;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.NotResumedException;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.spi.DataFormat;
import org.camunda.bpm.client.spi.DataFormatConfigurator;
import org.camunda.bpm.client.spi.DataFormatProvider;
import org.camunda.bpm.client.topic.impl.TopicSubscriptionManagerLogger;
import org.camunda.bpm.client.variable.impl.format.json.JacksonJsonLogger;
import org.camunda.bpm.client.variable.impl.format.serializable.SerializableLogger;
import org.camunda.bpm.client.variable.impl.format.xml.DomXmlLogger;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.commons.logging.BaseLogger;

/**
 * @author Tassilo Weidner
 */
public class ExternalTaskClientLogger extends BaseLogger {

  protected static final String PROJECT_CODE = "TASK/CLIENT";
  protected static final String PROJECT_LOGGER = "org.camunda.bpm.client";

  public static final ExternalTaskClientLogger CLIENT_LOGGER =
    createLogger(ExternalTaskClientLogger.class, PROJECT_CODE, PROJECT_LOGGER, "01");

  public static final EngineClientLogger ENGINE_CLIENT_LOGGER =
    createLogger(EngineClientLogger.class, PROJECT_CODE, PROJECT_LOGGER, "02");

  public static final TopicSubscriptionManagerLogger TOPIC_SUBSCRIPTION_MANAGER_LOGGER =
    createLogger(TopicSubscriptionManagerLogger.class, PROJECT_CODE, PROJECT_LOGGER, "03");

  public static final DomXmlLogger XML_FORMAT_LOGGER =
      createLogger(DomXmlLogger.class, PROJECT_CODE, PROJECT_LOGGER, "04");

  public static final JacksonJsonLogger JSON_FORMAT_LOGGER =
      createLogger(JacksonJsonLogger.class, PROJECT_CODE, PROJECT_LOGGER, "05");

  public static final SerializableLogger SERIALIZABLE_FORMAT_LOGGER =
      createLogger(SerializableLogger.class, PROJECT_CODE, PROJECT_LOGGER, "06");

  protected void logError(String id, String messageTemplate, Throwable t) {
    if (delegateLogger.isErrorEnabled()) {
      String msg = formatMessageTemplate(id, messageTemplate);
      delegateLogger.error(msg, t);
    }
  }

  protected void logInfo(String id, String messageTemplate, Throwable t) {
    if(delegateLogger.isInfoEnabled()) {
      String msg = formatMessageTemplate(id, messageTemplate);
      delegateLogger.info(msg, t);
    }
  }

  protected ExternalTaskClientException baseUrlNullException() {
    return new ExternalTaskClientException(exceptionMessage(
      "001", "Base URL cannot be null or an empty string"));
  }

  protected ExternalTaskClientException cannotGetHostnameException(Throwable cause) {
    return new ExternalTaskClientException(exceptionMessage("002", "Cannot get hostname"), cause);
  }

  public ExternalTaskClientException topicNameNullException() {
    return new ExternalTaskClientException(exceptionMessage(
      "003", "Topic name cannot be null"));
  }

  public ExternalTaskClientException lockDurationIsNotGreaterThanZeroException(Long lockDuration) {
    return new ExternalTaskClientException(exceptionMessage(
      "004", "Lock duration must be greater than 0, but was '{}'", lockDuration));
  }

  public ExternalTaskClientException externalTaskHandlerNullException() {
    return new ExternalTaskClientException(exceptionMessage(
      "005", "External task handler cannot be null"));
  }

  public ExternalTaskClientException topicNameAlreadySubscribedException(String topicName) {
    return new ExternalTaskClientException(exceptionMessage(
      "006", "Topic name '{}' has already been subscribed", topicName));
  }

  public ExternalTaskClientException externalTaskServiceException(String actionName, EngineClientException e) {
    Throwable causedException = e.getCause();

    if (causedException instanceof HttpResponseException) {
      switch (((HttpResponseException) causedException).getStatusCode()) {
        case 400:
          return new NotAcquiredException(exceptionMessage(
            "007", "Exception while {}: The task's most recent lock could not be acquired", actionName));
        case 404:
          return new NotFoundException(exceptionMessage(
            "008", "Exception while {}: The task could not be found", actionName));
        case 500:
          return new NotResumedException(exceptionMessage(
            "009", "Exception while {}: The corresponding process instance could not be resumed", actionName));
      }
    }

    if (causedException instanceof ClientProtocolException || causedException instanceof IOException) {
      return new ConnectionLostException(exceptionMessage(
        "010", "Exception while {}: Connection could not be established", actionName));
    }

    return new ExternalTaskClientException(exceptionMessage(
      "011", "Exception while {}: ", actionName), e);
  }

  public ExternalTaskClientException basicAuthCredentialsNullException() {
    return new ExternalTaskClientException(exceptionMessage(
      "012", "Basic authentication credentials (username, password) cannot be null"));
  }

  protected ExternalTaskClientException interceptorNullException() {
    return new ExternalTaskClientException(exceptionMessage(
      "013", "Interceptor cannot be null"));
  }

  public ExternalTaskClientException maxTasksNotGreaterThanZeroException(Integer maxTasks) {
    return new ExternalTaskClientException(exceptionMessage(
      "014", "Maximum amount of fetched tasks must be greater than zero, bus was '{}'", maxTasks));
  }

  public ExternalTaskClientException asyncResponseTimeoutNotGreaterThanZeroException(Long asyncResponseTimeout) {
    return new ExternalTaskClientException(exceptionMessage(
      "015", "Asynchronous response timeout must be greater than zero, but was '{}'", asyncResponseTimeout));
  }

  public ValueMapperException valueMapperExceptionWhileParsingDate(String date, Exception e) {
    return new ValueMapperException(exceptionMessage(
      "018", "Exception while mapping value: Cannot parse date '{}'", date), e);
  }

  public ValueMapperException valueMapperExceptionDueToNoObjectTypeName() {
    return new ValueMapperException(exceptionMessage(
      "019", "Exception while mapping value: " +
        "Cannot write serialized value for variable: no 'objectTypeName' provided for non-null value."));
  }

  public ValueMapperException valueMapperExceptionWhileSerializingObject(Exception e) {
    return new ValueMapperException(exceptionMessage(
      "020", "Exception while mapping value: Cannot serialize object in variable."), e);
  }

  public ValueMapperException valueMapperExceptionWhileDeserializingObject(Exception e) {
    return new ValueMapperException(exceptionMessage(
      "021", "Exception while mapping value: Cannot deserialize object in variable."), e);
  }

  public ValueMapperException valueMapperExceptionWhileSerializingAbstractValue(String name) {
    return new ValueMapperException(exceptionMessage(
      "022", "Cannot serialize value of abstract type '{}'", name));
  }

  public ValueMapperException valueMapperExceptionDueToSerializerNotFoundForTypedValue(TypedValue typedValue) {
    return new ValueMapperException(exceptionMessage(
      "023", "Cannot find serializer for value '{}'", typedValue));
  }

  public ValueMapperException valueMapperExceptionDueToSerializerNotFoundForTypedValueField(Object value) {
    return new ValueMapperException(exceptionMessage(
      "024", "Cannot find serializer for value '{}'", value));
  }

  public ValueMapperException cannotSerializeVariable(String variableName, Throwable e) {
    return new ValueMapperException(exceptionMessage("025", "Cannot serialize variable '{}'", variableName), e); 
  }

  public void logDataFormats(Collection<DataFormat> formats) {
    if (isInfoEnabled()) {
      for (DataFormat format : formats) {
        logDataFormat(format);
      }
    }
  }

  protected void logDataFormat(DataFormat dataFormat) {
    logInfo("025", "Discovered data format: {}[name = {}]", dataFormat.getClass().getName(), dataFormat.getName());
  }

  public void logDataFormatProvider(DataFormatProvider provider) {
    if (isInfoEnabled()) {
      logInfo("026", "Discovered data format provider: {}[name = {}]",
          provider.getClass().getName(), provider.getDataFormatName());
    }
  }

  @SuppressWarnings("rawtypes")
  public void logDataFormatConfigurator(DataFormatConfigurator configurator) {
    if (isInfoEnabled()) {
      logInfo("027", "Discovered data format configurator: {}[dataformat = {}]",
          configurator.getClass(), configurator.getDataFormatClass().getName());
    }
  }

  public ExternalTaskClientException multipleProvidersForDataformat(String dataFormatName) {
    return new ExternalTaskClientException(exceptionMessage("028", "Multiple providers found for dataformat '{}'", dataFormatName));
  }

  public ExternalTaskClientException cannotLoadDeferedFileValueException(String variableName, Exception e) {
    return new ExternalTaskClientException(exceptionMessage("029", "Variable '{}' of type file could not be retrieved", variableName), e);
  }

  public ExternalTaskClientException passNullValueParameter(String parameterName) {
    return new ExternalTaskClientException(exceptionMessage(
        "030", "Null value is not allowed as '{}'", parameterName));
  }

}
