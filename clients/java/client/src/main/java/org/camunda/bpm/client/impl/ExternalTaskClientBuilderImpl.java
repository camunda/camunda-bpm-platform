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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.camunda.bpm.client.ClientBackOffStrategy;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.impl.variable.TypedValues;
import org.camunda.bpm.client.impl.variable.ValueMappers;
import org.camunda.bpm.client.impl.variable.mapper.DefaultValueMappers;
import org.camunda.bpm.client.impl.variable.mapper.ValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.BooleanValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.ByteArrayValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.DateValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.DoubleValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.IntegerValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.LongValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.NullValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.ShortValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.StringValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.serializable.JavaObjectMapper;
import org.camunda.bpm.client.impl.variable.mapper.serializable.JsonValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.serializable.SpinObjectValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.serializable.XmlValueMapper;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.impl.RequestInterceptorHandler;
import org.camunda.bpm.client.topic.impl.TopicSubscriptionManager;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.DataFormats;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.xml.SpinXmlElement;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tassilo Weidner
 */
public class ExternalTaskClientBuilderImpl implements ExternalTaskClientBuilder {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected String baseUrl;
  protected String workerId;
  protected int maxTasks;
  protected Long asyncResponseTimeout;
  protected long lockDuration;

  protected String defaultSerializationFormat = Variables.SerializationDataFormats.JAVA.getName();

  protected String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  protected ObjectMapper objectMapper;
  protected ValueMappers valueMappers;
  protected TypedValues typedValues;
  protected EngineClient engineClient;
  protected TopicSubscriptionManager topicSubscriptionManager;

  protected List<ClientRequestInterceptor> interceptors;
  protected boolean isAutoFetchingEnabled;
  protected ClientBackOffStrategy backOffStrategy;

  public ExternalTaskClientBuilderImpl() {
    // default values
    this.maxTasks = 10;
    this.asyncResponseTimeout = null;
    this.lockDuration = 20_000;
    this.isAutoFetchingEnabled = true;
    this.interceptors = new ArrayList<>();
  }

  public ExternalTaskClientBuilder baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public ExternalTaskClientBuilder workerId(String workerId) {
    this.workerId = workerId;
    return this;
  }

  public ExternalTaskClientBuilder addInterceptor(ClientRequestInterceptor interceptor) {
    this.interceptors.add(interceptor);
    return this;
  }

  public ExternalTaskClientBuilder maxTasks(int maxTasks) {
    this.maxTasks = maxTasks;
    return this;
  }

  public ExternalTaskClientBuilder asyncResponseTimeout(long asyncResponseTimeout) {
    this.asyncResponseTimeout = asyncResponseTimeout;
    return this;
  }

  public ExternalTaskClientBuilder lockDuration(long lockDuration) {
    this.lockDuration = lockDuration;
    return this;
  }

  public ExternalTaskClientBuilder disableAutoFetching() {
    this.isAutoFetchingEnabled = false;
    return this;
  }

  public ExternalTaskClientBuilder backOff(ClientBackOffStrategy backOffStrategy) {
    this.backOffStrategy = backOffStrategy;
    return this;
  }

  public ExternalTaskClientBuilder defaultSerializationFormat(String defaultSerializationFormat) {
    this.defaultSerializationFormat = defaultSerializationFormat;
    return this;
  }

  public ExternalTaskClientBuilder dateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
    return this;
  }

  public ExternalTaskClient build() {
    if (maxTasks <= 0) {
      throw LOG.maxTasksNotGreaterThanZeroException();
    }

    if (asyncResponseTimeout != null && asyncResponseTimeout <= 0) {
      throw LOG.asyncResponseTimeoutNotGreaterThanZeroException();
    }

    if (lockDuration <= 0L) {
      throw LOG.lockDurationIsNotGreaterThanZeroException();
    }

    if (baseUrl == null || baseUrl.isEmpty()) {
      throw LOG.baseUrlNullException();
    }

    checkInterceptors();

    initBaseUrl();
    initWorkerId();
    initObjectMapper();
    initVariableMappers();
    initEngineClient();
    initTopicSubscriptionManager();

    return new ExternalTaskClientImpl(topicSubscriptionManager);
  }

  protected void initBaseUrl() {
    baseUrl = sanitizeUrl(baseUrl);
  }

  protected String sanitizeUrl(String url) {
    url = url.trim();
    if (url.endsWith("/")) {
      url = url.replaceAll("/$", "");
      url = sanitizeUrl(url);
    }
    return url;
  }

  protected void initWorkerId() {
    if (workerId == null) {
      String hostname = checkHostname();
      this.workerId = hostname + UUID.randomUUID();
    }
  }

  protected void checkInterceptors() {
    interceptors.forEach(interceptor -> {
      if (interceptor == null) {
        throw LOG.interceptorNullException();
      }
    });
  }

  protected void initObjectMapper() {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
  }

  protected void initVariableMappers() {
    valueMappers = new DefaultValueMappers(defaultSerializationFormat);

    valueMappers.addSerializer(new NullValueMapper());
    valueMappers.addSerializer(new BooleanValueMapper());
    valueMappers.addSerializer(new StringValueMapper());
    valueMappers.addSerializer(new DateValueMapper(dateFormat));
    valueMappers.addSerializer(new ByteArrayValueMapper());

    // number mappers
    valueMappers.addSerializer(new IntegerValueMapper());
    valueMappers.addSerializer(new LongValueMapper());
    valueMappers.addSerializer(new ShortValueMapper());
    valueMappers.addSerializer(new DoubleValueMapper());

    // spin
    List<ValueMapper<?>> spinMappers = lookupSpinDataformats();
    spinMappers.forEach(valueMappers::addSerializer);

    // default object serialization
    valueMappers.addSerializer(new JavaObjectMapper());
    typedValues = new TypedValues(valueMappers);
  }

  protected void initEngineClient() {
    RequestInterceptorHandler requestInterceptorHandler = new RequestInterceptorHandler(interceptors);
    RequestExecutor requestExecutor = new RequestExecutor(requestInterceptorHandler, objectMapper);
    engineClient = new EngineClient(workerId, maxTasks, asyncResponseTimeout, baseUrl, requestExecutor, typedValues);
  }

  protected void initTopicSubscriptionManager() {
    topicSubscriptionManager = new TopicSubscriptionManager(engineClient, typedValues, lockDuration);

    if (getBackOffStrategy() != null) {
      topicSubscriptionManager.setBackOffStrategy(getBackOffStrategy());
    }

    if (isAutoFetchingEnabled()) {
      topicSubscriptionManager.start();
    }

  }

  @SuppressWarnings("unchecked")
  protected List<ValueMapper<?>> lookupSpinDataformats() {
    List<ValueMapper<?>> serializers = new ArrayList<ValueMapper<?>>();

    if (isDataFormatsAvailable()) {

      // TODO: log that spin dataformat has been detected

      Set<DataFormat<?>> availableDataFormats = DataFormats.getAvailableDataFormats();
      for (DataFormat<?> dataFormat : availableDataFormats) {
        serializers.add(new SpinObjectValueMapper("spin://"+dataFormat.getName(), dataFormat));
      }

      if (isSpinValuesAvailable()) {
        DataFormats dataFormats = DataFormats.getInstance();
        if(dataFormats.getDataFormatByName(DataFormats.JSON_DATAFORMAT_NAME) != null) {
          DataFormat<SpinJsonNode> jsonDataFormat = (DataFormat<SpinJsonNode>) dataFormats.getDataFormatByName(DataFormats.JSON_DATAFORMAT_NAME);
          serializers.add(new JsonValueMapper(jsonDataFormat));
        }
        if(dataFormats.getDataFormatByName(DataFormats.XML_DATAFORMAT_NAME) != null){
          DataFormat<SpinXmlElement> xmlDataFormat = (DataFormat<SpinXmlElement>) dataFormats.getDataFormatByName(DataFormats.XML_DATAFORMAT_NAME);
          serializers.add(new XmlValueMapper(xmlDataFormat));
        }

      }
    }

    return serializers;
  }


  protected boolean isDataFormatsAvailable() {
    boolean isAvailable = false;

    try {
      Class.forName("org.camunda.spin.DataFormats");
      isAvailable = true;
    } catch (ClassNotFoundException e) {
      // TODO: Log that spin data formats are not available
    }

    return isAvailable;
  }

  protected boolean isSpinValuesAvailable() {
    boolean isAvailable = false;

    try {
      Class.forName("org.camunda.spin.plugin.variable.SpinValues");
      isAvailable = true;
    } catch (ClassNotFoundException e) {
      // TODO: Log that spin data formats are not available
    }

    return isAvailable;
  }

  public String checkHostname() {
    String hostname;
    try {
      hostname = getHostname();
    } catch (UnknownHostException e) {
      throw LOG.cannotGetHostnameException();
    }

    return hostname;
  }

  public String getHostname() throws UnknownHostException {
    return InetAddress.getLocalHost().getHostName();
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  protected String getWorkerId() {
    return workerId;
  }

  protected List<ClientRequestInterceptor> getInterceptors() {
    return interceptors;
  }

  protected int getMaxTasks() {
    return maxTasks;
  }

  protected Long getAsyncResponseTimeout() {
    return asyncResponseTimeout;
  }

  protected long getLockDuration() {
    return lockDuration;
  }

  protected boolean isAutoFetchingEnabled() {
    return isAutoFetchingEnabled;
  }

  protected ClientBackOffStrategy getBackOffStrategy() {
    return backOffStrategy;
  }

  public String getDefaultSerializationFormat() {
    return defaultSerializationFormat;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public ValueMappers getValueMappers() {
    return valueMappers;
  }

  public TypedValues getTypedValues() {
    return typedValues;
  }

  public EngineClient getEngineClient() {
    return engineClient;
  }

}
