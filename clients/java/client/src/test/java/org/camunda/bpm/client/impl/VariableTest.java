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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.plugin.variable.SpinValues.jsonValue;
import static org.camunda.spin.plugin.variable.SpinValues.xmlValue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.exception.UnknownTypeException;
import org.camunda.bpm.client.exception.UnsupportedTypeException;
import org.camunda.bpm.client.helper.ClosableHttpClientMock;
import org.camunda.bpm.client.helper.MockProvider;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.task.impl.dto.CompleteRequestDto;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.client.topic.TopicSubscriptionBuilder;
import org.camunda.bpm.client.topic.impl.dto.FetchAndLockRequestDto;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.NullValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.ShortValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.camunda.spin.impl.xml.dom.DomXmlElement;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tassilo Weidner
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class, ExternalTaskClientImpl.class})
public class VariableTest {

  private CloseableHttpResponse closeableHttpResponse;

  @Before
  public void setUp() throws JsonProcessingException {
    mockStatic(HttpClients.class);

    HttpClientBuilder httpClientBuilderMock = mock(HttpClientBuilder.class, RETURNS_DEEP_STUBS);
    when(HttpClients.custom())
      .thenReturn(httpClientBuilderMock);

    closeableHttpResponse = mock(CloseableHttpResponse.class);
    when(closeableHttpResponse.getStatusLine())
      .thenReturn(mock(StatusLine.class));

    CloseableHttpClient httpClient = spy(new ClosableHttpClientMock(closeableHttpResponse));
    when(httpClientBuilderMock.build())
      .thenReturn(httpClient);
  }
  
  /* tests if response of fetch and lock is deserialized properly */
  
  @Test
  public void shouldRetrieveAllVariablesUntypedFromEngine() throws JsonProcessingException {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTask()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<ExternalTask> externalTaskReference = new ArrayList<>(); // list, as container must be final and changeable

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          externalTaskReference.add(externalTask);
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    ExternalTask externalTask = externalTaskReference.get(0);

    assertAllVariablesUntyped(externalTask);
  }

  @Test
  public void shouldRetrieveAllVariablesTypedFromEngine() throws JsonProcessingException {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTask()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<ExternalTask> externalTaskReference = new ArrayList<>(); // list, as container must be final and changeable

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          externalTaskReference.add(externalTask);
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    ExternalTask externalTask = externalTaskReference.get(0);

    assertAllVariablesTyped(externalTask);
  }

  @Test
  public void shouldRetrieveSingleVariableUntypedFromEngine() throws JsonProcessingException {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTask()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<ExternalTask> externalTaskReference = new ArrayList<>(); // list, as container must be final and changeable

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          externalTaskReference.add(externalTask);
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    ExternalTask externalTask = externalTaskReference.get(0);

    assertSingleVariableUntyped(externalTask);
  }

  @Test
  public void shouldRetrieveSingleVariableTypedFromEngine() throws JsonProcessingException {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTask()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<ExternalTask> externalTaskReference = new ArrayList<>(); // list, as container must be final and changeable

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          externalTaskReference.add(externalTask);
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    ExternalTask externalTask = externalTaskReference.get(0);

    assertSingleVariableTyped(externalTask);
  }

  /* test if complete request is serialized properly */
  
  @Test
  public void shouldSetAllVariablesUntypedAccordingToCompleteRequest() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          Map<String, Object> untypedVariables = new HashMap<>();
          untypedVariables.put(MockProvider.BOOLEAN_VARIABLE_NAME, MockProvider.BOOLEAN_VARIABLE_VALUE);
          untypedVariables.put(MockProvider.SHORT_VARIABLE_NAME, MockProvider.SHORT_VARIABLE_VALUE);
          untypedVariables.put(MockProvider.INTEGER_VARIABLE_NAME, MockProvider.INTEGER_VARIABLE_VALUE);
          untypedVariables.put(MockProvider.LONG_VARIABLE_NAME, MockProvider.LONG_VARIABLE_VALUE);
          untypedVariables.put(MockProvider.STRING_VARIABLE_NAME, MockProvider.STRING_VARIABLE_VALUE);
          untypedVariables.put(MockProvider.DOUBLE_VARIABLE_NAME, MockProvider.DOUBLE_VARIABLE_VALUE);
          untypedVariables.put(MockProvider.DATE_VARIABLE_NAME, MockProvider.DATE_VARIABLE_VALUE);
          untypedVariables.put(MockProvider.BYTES_VARIABLE_NAME, MockProvider.BYTES_VARIABLE_VALUE);
          untypedVariables.put(MockProvider.NULL_VARIABLE_NAME, null);

          externalTaskService.complete(externalTask, untypedVariables);

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertCompleteRequestSerialization(objectMapper);
  }

  @Test
  public void shouldSetAllVariablesTypedAccordingToCompleteRequest() throws Exception {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          Map<String, Object> typedVariables = new HashMap<>();
          typedVariables.put(MockProvider.BOOLEAN_VARIABLE_NAME, Variables.booleanValue(MockProvider.BOOLEAN_VARIABLE_VALUE));
          typedVariables.put(MockProvider.SHORT_VARIABLE_NAME, Variables.shortValue(MockProvider.SHORT_VARIABLE_VALUE));
          typedVariables.put(MockProvider.INTEGER_VARIABLE_NAME, Variables.integerValue(MockProvider.INTEGER_VARIABLE_VALUE));
          typedVariables.put(MockProvider.LONG_VARIABLE_NAME, Variables.longValue(MockProvider.LONG_VARIABLE_VALUE));
          typedVariables.put(MockProvider.DOUBLE_VARIABLE_NAME, Variables.doubleValue(MockProvider.DOUBLE_VARIABLE_VALUE));
          typedVariables.put(MockProvider.STRING_VARIABLE_NAME, Variables.stringValue(MockProvider.STRING_VARIABLE_VALUE));
          typedVariables.put(MockProvider.DATE_VARIABLE_NAME, Variables.dateValue(MockProvider.DATE_VARIABLE_VALUE));
          typedVariables.put(MockProvider.BYTES_VARIABLE_NAME, Variables.byteArrayValue(MockProvider.BYTES_VARIABLE_VALUE));
          typedVariables.put(MockProvider.NULL_VARIABLE_NAME, Variables.untypedNullValue());

          externalTaskService.complete(externalTask, typedVariables);

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertCompleteRequestSerialization(objectMapper);
  }

  /* tests if exceptions are thrown correctly */

  @Test
  public void shouldThrowUnsupportedTypeException() throws JsonProcessingException {
    // given
    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTask()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<UnsupportedTypeException> exceptionReference = new ArrayList<>(); // list, as container must be final and changeable

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          try {
            externalTaskService.complete(externalTask, null, Collections.singletonMap("aVariableName", new ExternalTaskImpl()));
          } catch (UnsupportedTypeException e) {
            exceptionReference.add(e);
          }

          try {
            externalTaskService.complete(externalTask, Collections.singletonMap("aVariableName", new ExternalTaskImpl()));
          } catch (UnsupportedTypeException e) {
            exceptionReference.add(e);
          }

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertThat(exceptionReference.get(0).getMessage().contains("no suitable mapper found for type ExternalTaskImpl"));
    assertThat(exceptionReference.get(1).getMessage().contains("no suitable mapper found for type ExternalTaskImpl"));
    assertThat(exceptionReference.get(2).getMessage().contains("no suitable mapper found for type ExternalTaskImpl"));
    assertThat(exceptionReference.get(3).getMessage().contains("no suitable mapper found for type ExternalTaskImpl"));
  }

  @Test
  public void shouldNotInvokeHandlerDueToTypeOfResponseAndValueDiffer() throws JsonProcessingException, InterruptedException {
    // given
    ExternalTask externalTask = MockProvider.createExternalTaskWithoutVariables();
    ((ExternalTaskImpl)externalTask).setVariables(Collections.singletonMap("aVariableName", createTypedValueDto("aWrongVariableValue", "Long")));
    mockFetchAndLockResponse(Collections.singletonList(externalTask));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();


    client.stop();

    // then
    verifyZeroInteractions(externalTaskHandlerMock);
  }

  /* tests for transient variables */

  @Test
  public void shouldDeserializeTransientVariables() throws JsonProcessingException {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    TypedValueDto typedValueDtoTransient = createTypedValueDto("aVariableValue", "String", true);
    TypedValueDto typedValueDtoNotTransient = createTypedValueDto("aVariableValue", "String");

    Map<String, TypedValueDto> typedValueDtoMap = new HashMap<>();
    typedValueDtoMap.put("aVariableName", typedValueDtoTransient);
    typedValueDtoMap.put("anotherVariableName", typedValueDtoNotTransient);

    ((ExternalTaskImpl)externalTaskMock).setVariables(typedValueDtoMap);
    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<ExternalTask> externalTaskReference = new ArrayList<>(); // list, as container must be final and changeable

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          externalTaskReference.add(externalTask);

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    ExternalTask externalTask = externalTaskReference.get(0);
    assertThat(externalTask.getVariableTyped("aVariableName").isTransient()).isEqualTo(true);
    assertThat(externalTask.getVariableTyped("anotherVariableName").isTransient()).isEqualTo(false);
  }

  @Test
  public void shouldSerializeTransientVariables() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          Map<String, Object> variables = new HashMap<>();
          variables.put("aVariableName", Variables.stringValue("aVariableValue", true));
          variables.put("anotherVariableName", Variables.stringValue("aVariableValue", true));
          externalTaskService.complete(externalTask, variables);

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    TypedValueDto typedValueDtoTransient = createTypedValueDto("aVariableValue", "String", true);
    TypedValueDto typedValueDtoNotTransient = createTypedValueDto("aVariableValue", "String", true);

    Map<String, TypedValueDto> expectedValueDtoMap = new HashMap<>();
    expectedValueDtoMap.put("aVariableName", typedValueDtoTransient);
    expectedValueDtoMap.put("anotherVariableName", typedValueDtoNotTransient);

    assertVariablePayloadOfCompleteRequest(objectMapper, expectedValueDtoMap);
  }

  @Test
  public void shouldSerializeTransientVariableOfTypeNullValue() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          Map<String, Object> variables = new HashMap<>();
          variables.put("aVariableName", Variables.untypedNullValue( true));
          variables.put("anotherVariableName", Variables.untypedNullValue( false));
          externalTaskService.complete(externalTask, variables);

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    TypedValueDto typedValueDtoTransient = createTypedValueDto(null, "Null", true);
    TypedValueDto typedValueDtoNotTransient = createTypedValueDto(null, "Null", false);

    Map<String, TypedValueDto> expectedValueDtoMap = new HashMap<>();
    expectedValueDtoMap.put("aVariableName", typedValueDtoTransient);
    expectedValueDtoMap.put("anotherVariableName", typedValueDtoNotTransient);

    assertVariablePayloadOfCompleteRequest(objectMapper, expectedValueDtoMap);
  }

  /* tests for object typed variables */

  @Test
  public void shouldDeserializeObjectTypedVariable() throws IOException {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    TypedValueDto typedValueDtoJsonObject = createTypedValueDto("[1, 2, 3, 4, 5]",
      "Object", "java.util.ArrayList", Variables.SerializationDataFormats.JSON.getName());

    TypedValueDto typedValueDtoJavaObject = createTypedValueDto(encodeObjectToBase64(new ArrayList<>(Arrays.asList(1,2,3,4,5))),
      "Object", "java.util.ArrayList", Variables.SerializationDataFormats.JAVA.getName());

    TypedValueDto typedValueDtoXmlObject = createTypedValueDto("<xmlList><e>1</e><e>2</e><e>3</e><e>4</e><e>5</e></xmlList>",
      "Object", XmlList.class.getTypeName(), Variables.SerializationDataFormats.XML.getName());

    Map<String, TypedValueDto> typedValueDtoMap = new HashMap<>();
    typedValueDtoMap.put("aVariableName", typedValueDtoJsonObject);
    typedValueDtoMap.put("anotherVariableName", typedValueDtoJavaObject);
    typedValueDtoMap.put("aXmlVariableName", typedValueDtoXmlObject);
    ((ExternalTaskImpl)externalTaskMock).setVariables(typedValueDtoMap);

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<ExternalTask> externalTaskReference = new ArrayList<>(); // list, as container must be final and changeable

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          externalTaskReference.add(externalTask);

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    ExternalTask externalTask = externalTaskReference.get(0);

    ObjectValue aVariableName = externalTask.getVariableTyped("aVariableName");
    assertObjectValue(aVariableName, ArrayList.class, Variables.SerializationDataFormats.JSON.getName());
    assertThat((List<Integer>) aVariableName.getValue()).containsOnly(1, 2, 3, 4, 5);

    ObjectValue anotherVariableName = externalTask.getVariableTyped("anotherVariableName");
    assertObjectValue(anotherVariableName, ArrayList.class, Variables.SerializationDataFormats.JAVA.getName());
    assertThat((List<Integer>) anotherVariableName.getValue()).containsOnly(1, 2, 3, 4, 5);

    ObjectValue aXmlVariableName = externalTask.getVariableTyped("aXmlVariableName");
    assertObjectValue(aXmlVariableName, XmlList.class, Variables.SerializationDataFormats.XML.getName());
    assertThat((XmlList) aXmlVariableName.getValue()).containsOnly(1, 2, 3, 4, 5);
  }

  @Test
  public void shouldThrowExceptionDueToUnknownTypeWhileDeserializingJsonObjectTypedVariable() throws JsonProcessingException, InterruptedException {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    TypedValueDto typedValueDtoObject = createTypedValueDto("[1, 2, 3, 4, 5]",
      "Object", "#§#4431%%", Variables.SerializationDataFormats.JSON.getName());

    ((ExternalTaskImpl)externalTaskMock).setVariables(Collections.singletonMap("aVariableName", typedValueDtoObject));

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();


    // then
    verifyNoMoreInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToUnknownTypeWhileDeserializingJavaObjectTypedVariable() throws IOException, InterruptedException {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    TypedValueDto typedValueDtoObject = createTypedValueDto(encodeObjectToBase64(new ArrayList<>(Arrays.asList(1,2,3,4,5))),
      "Object", "#§#4431%%", Variables.SerializationDataFormats.JAVA.getName());

    ((ExternalTaskImpl)externalTaskMock).setVariables(Collections.singletonMap("aVariableName", typedValueDtoObject));

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();


    // then
    verifyNoMoreInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToUnknownTypeWhileDeserializingXmlObjectTypedVariable() throws IOException, InterruptedException {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    TypedValueDto typedValueDtoObject = createTypedValueDto("<xmlList><e>1</e><e>2</e><e>3</e><e>4</e><e>5</e></xmlList>",
      "Object", "#§#4431%%", Variables.SerializationDataFormats.XML.getName());

    ((ExternalTaskImpl)externalTaskMock).setVariables(Collections.singletonMap("aVariableName", typedValueDtoObject));

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();


    // then
    verifyNoMoreInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToInvalidJsonSerializationDataWhileDeserializingObjectTypedVariable() throws JsonProcessingException, InterruptedException {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    TypedValueDto typedValueDtoObject = createTypedValueDto("[1, 2, 3, 4, 5",
      "Object", "java.util.ArrayList", Variables.SerializationDataFormats.JSON.getName());

    ((ExternalTaskImpl)externalTaskMock).setVariables(Collections.singletonMap("aVariableName", typedValueDtoObject));

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();


    // then
    verifyNoMoreInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToInvalidXmlSerializationDataWhileDeserializingObjectTypedVariable() throws JsonProcessingException, InterruptedException {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    TypedValueDto typedValueDtoObject = createTypedValueDto("<xmlList><e>1</e><e>2</e><e>3</e><e>4</e><e>5</e></xmlList",
      "Object", XmlList.class.getTypeName(), Variables.SerializationDataFormats.XML.getName());

    ((ExternalTaskImpl)externalTaskMock).setVariables(Collections.singletonMap("aVariableName", typedValueDtoObject));

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();


    // then
    verifyNoMoreInteractions(externalTaskHandlerMock);
  }

  @Test
  public void shouldThrowExceptionDueToInvalidJavaSerializationDataWhileDeserializingObjectTypedVariable() throws IOException, InterruptedException {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    TypedValueDto typedValueDtoObject = createTypedValueDto("no base64",
      "Object", "java.util.ArrayList", Variables.SerializationDataFormats.JAVA.getName());

    ((ExternalTaskImpl)externalTaskMock).setVariables(Collections.singletonMap("aVariableName", typedValueDtoObject));

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    ExternalTaskHandler externalTaskHandlerMock = mock(ExternalTaskHandler.class);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler(externalTaskHandlerMock);

    // when
    topicSubscriptionBuilder.open();


    // then
    verifyNoMoreInteractions(externalTaskHandlerMock);
  }

  @Test
  @Ignore("CAM-8883")
  public void shouldSerializeObjectTypedVariableWithDefaultDataSerializationFormat() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          ObjectValue objectValue = Variables
            .objectValue(new ArrayList<>(Arrays.asList(1,2,3,4,5)))
            .create();

          externalTaskService.complete(externalTask, Collections.singletonMap("aVariableName", objectValue));

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertVariablePayloadOfCompleteRequest(objectMapper, Collections.singletonMap("aVariableName",
      createTypedValueDto("[1,2,3,4,5]", "Object",
        ArrayList.class.getTypeName(), Variables.SerializationDataFormats.JSON.getName())));
  }

  @Test
  @Ignore("CAM-8883")
  public void shouldSerializeObjectTypedVariableWithJsonDataSerializationFormat() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          ObjectValue objectValue = Variables
            .objectValue(new ArrayList<>(Arrays.asList(1,2,3,4,5)))
            .serializationDataFormat(Variables.SerializationDataFormats.JSON.getName())
            .create();

          externalTaskService.complete(externalTask, Collections.singletonMap("aVariableName", objectValue));

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertVariablePayloadOfCompleteRequest(objectMapper, Collections.singletonMap("aVariableName",
      createTypedValueDto("[1,2,3,4,5]", "Object",
        ArrayList.class.getTypeName(), Variables.SerializationDataFormats.JSON.getName())));
  }

  @Test
  @Ignore("CAM-8883")
  public void shouldSerializeObjectTypedVariableWithJavaDataSerializationFormat() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          ObjectValue objectValue = Variables
            .objectValue(new ArrayList<>(Arrays.asList(1,2,3,4,5)))
            .serializationDataFormat(Variables.SerializationDataFormats.JAVA.getName())
            .create();

          externalTaskService.complete(externalTask, Collections.singletonMap("aVariableName", objectValue));

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertVariablePayloadOfCompleteRequest(objectMapper, Collections.singletonMap("aVariableName",
      createTypedValueDto(encodeObjectToBase64(new ArrayList<>(Arrays.asList(1,2,3,4,5))), "Object",
        ArrayList.class.getTypeName(), Variables.SerializationDataFormats.JSON.getName())));
  }

  @Test
  @Ignore("CAM-8883")
  public void shouldSerializeObjectTypedVariableWithXmlDataSerializationFormat() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          ObjectValue objectValue = Variables
            .objectValue(new XmlList(Arrays.asList(1,2,3,4,5)))
            .serializationDataFormat(Variables.SerializationDataFormats.XML.getName())
            .create();

          externalTaskService.complete(externalTask, Collections.singletonMap("aVariableName", objectValue));

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertVariablePayloadOfCompleteRequest(objectMapper, Collections.singletonMap("aVariableName",
      createTypedValueDto("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xmlList>\n  <e>1</e>\n  <e>2</e>\n  " +
        "<e>3</e>\n  <e>4</e>\n  <e>5</e>\n</xmlList>\n", "Object", ArrayList.class.getTypeName(),
        Variables.SerializationDataFormats.XML.getName())));
  }

  @Test
  public void shouldThrowUnknownTypeExceptionDueToUnknownObjectTypeWithDefaultDataSerializationFormat() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    List<UnknownTypeException> unknownTypeExceptionReference = new ArrayList<>();

    class Unknown {}

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          ObjectValue objectValue = Variables
            .objectValue(new Unknown())
            .create();

          try {
            externalTaskService.complete(externalTask, Collections.singletonMap("aVariableName", objectValue));
            fail("No UnknownTypeException thrown!");
          } catch (UnknownTypeException e) {
            unknownTypeExceptionReference.add(e);
            handlerInvoked.set(true);
          }
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    UnknownTypeException unknownTypeException = unknownTypeExceptionReference.get(0);
    assertThat(unknownTypeException).hasMessageContaining("the type of the object is not on the class path");
  }

  @Test
  public void shouldThrowUnknownTypeExceptionDueToUnknownObjectTypeWithJavaDataSerializationFormat() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    List<UnknownTypeException> unknownTypeExceptionReference = new ArrayList<>();

    class Unknown {}

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          ObjectValue objectValue = Variables
            .objectValue(new Unknown())
            .serializationDataFormat(Variables.SerializationDataFormats.JAVA.getName())
            .create();

          try {
            externalTaskService.complete(externalTask, Collections.singletonMap("aVariableName", objectValue));
            fail("No UnknownTypeException thrown!");
          } catch (UnknownTypeException e) {
            unknownTypeExceptionReference.add(e);
            handlerInvoked.set(true);
          }
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    UnknownTypeException unknownTypeException = unknownTypeExceptionReference.get(0);
    assertThat(unknownTypeException).hasMessageContaining("the type of the object is not on the class path");
  }

  @Test
  public void shouldThrowUnknownTypeExceptionDueToUnknownObjectTypeWithXmlDataSerializationFormat() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    List<UnknownTypeException> unknownTypeExceptionReference = new ArrayList<>();

    class Unknown {}

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          ObjectValue objectValue = Variables
            .objectValue(new Unknown())
            .serializationDataFormat(Variables.SerializationDataFormats.XML.getName())
            .create();

          try {
            externalTaskService.complete(externalTask, Collections.singletonMap("aVariableName", objectValue));
            fail("No UnknownTypeException thrown!");
          } catch (UnknownTypeException e) {
            unknownTypeExceptionReference.add(e);
            handlerInvoked.set(true);
          }
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    UnknownTypeException unknownTypeException = unknownTypeExceptionReference.get(0);
    assertThat(unknownTypeException).hasMessageContaining("the type of the object is not on the class path");
  }

  @Test
  public void shouldThrowUnsupportedTypeExceptionDueToUnknownDataSerializationFormat() throws Exception {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    List<UnsupportedTypeException> unsupportedTypeExceptionReference = new ArrayList<>();

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          ObjectValue objectValue = Variables
            .objectValue(new ArrayList<>(Arrays.asList(1,2,3,4,5)))
            .serializationDataFormat("unknown serialization data format")
            .create();

          try {
            externalTaskService.complete(externalTask, Collections.singletonMap("aVariableName", objectValue));
            fail("No UnsupportedTypeException thrown!");
          } catch (UnsupportedTypeException e) {
            unsupportedTypeExceptionReference.add(e);
            handlerInvoked.set(true);
          }
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    UnsupportedTypeException unsupportedTypeException = unsupportedTypeExceptionReference.get(0);
    assertThat(unsupportedTypeException).hasMessageContaining("Exception while converting variable value");
  }

  /* tests for spin types */

  @Test
  public void shouldDeserializeSpinTypes() throws IOException {
    // given
    ExternalTask externalTaskMock = MockProvider.createExternalTaskWithoutVariables();

    TypedValueDto typedValueDtoJson = createTypedValueDto("[1, 2, 3, 4, 5]", "Json");
    TypedValueDto typedValueDtoXml = createTypedValueDto("<entry>hello world</entry>", "Xml");

    Map<String, TypedValueDto> typedValueDtoMap = new HashMap<>();
    typedValueDtoMap.put("aJsonVariable", typedValueDtoJson);
    typedValueDtoMap.put("aXmlVariable", typedValueDtoXml);
    ((ExternalTaskImpl)externalTaskMock).setVariables(typedValueDtoMap);

    mockFetchAndLockResponse(Collections.singletonList(externalTaskMock));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    final List<ExternalTask> externalTaskReference = new ArrayList<>(); // list, as container must be final and changeable

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          externalTaskReference.add(externalTask);

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    ExternalTask externalTask = externalTaskReference.get(0);

    assertSpinVariableValue(externalTask, "aJsonVariable", "[1,2,3,4,5]", "Json");
    assertSpinVariableValue(externalTask, "aXmlVariable", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><entry>hello world</entry>\n", "Xml");
  }

  @Test
  public void shouldSetSpinTypes() throws Exception {
    // given
    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);

    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          Map<String, Object> variables = new HashMap<>();
          variables.put("aJsonVariable", jsonValue("[1,2,3,4,5]").create());
          variables.put("aXmlVariable", xmlValue("<entry>hello world</entry>").create());

          externalTaskService.complete(externalTask, variables);

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    Map<String, TypedValueDto> expectedVariables = new HashMap<>();
    expectedVariables.put("aJsonVariable", createTypedValueDto("[1,2,3,4,5]", "Json"));
    expectedVariables.put("aXmlVariable", createTypedValueDto("<?xml version=\"1.0\" encoding=\"UTF-8\"?><entry>hello world</entry>\n", "Xml"));
    assertVariablePayloadOfCompleteRequest(objectMapper, expectedVariables);
  }

  /* tests for local variables */

  @Test
  public void shouldCompleteWithLocalVariables() throws Exception {
    // given
    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          Map<String, Object> localVariables = new HashMap<>();
          localVariables.put("aVariableName", Variables.stringValue("aVariableValue"));
          localVariables.put("anotherVariableName", 47L);

          externalTaskService.complete(externalTask, null, localVariables);

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    Map<String, TypedValueDto> expectedVariableDtoMap = new HashMap<>();
    expectedVariableDtoMap.put("aVariableName", createTypedValueDto("aVariableValue", MockProvider.STRING_VARIABLE_TYPE));
    expectedVariableDtoMap.put("anotherVariableName", createTypedValueDto( 47L, MockProvider.LONG_VARIABLE_TYPE));

    assertLocalVariablePayloadOfCompleteRequest(objectMapper, expectedVariableDtoMap);
  }

  @Test
  public void shouldCompleteWithBothVariablesAndLocalVariables() throws Exception {
    // given
    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {

          externalTaskService.complete(externalTask, Collections.singletonMap("anotherVariableName", 47L),
            Collections.singletonMap("aVariableName", Variables.stringValue("aVariableValue")));

          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertLocalVariablePayloadOfCompleteRequest(objectMapper, Collections.singletonMap("aVariableName",
      createTypedValueDto("aVariableValue", MockProvider.STRING_VARIABLE_TYPE)));
    assertVariablePayloadOfCompleteRequest(objectMapper, Collections.singletonMap("anotherVariableName",
      createTypedValueDto(47L, MockProvider.LONG_VARIABLE_TYPE)));
  }

  /* tests if variables filter is applied */
  @Test
  public void shouldApplyVariablesFilter() throws Exception {
    // given
    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .variables("aVariableName", "anotherVariableName")
        .handler((externalTask, externalTaskService) -> {
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertVariablesFilterAppliedAccordingToFetchAndLockPayload(objectMapper, "aVariableName", "anotherVariableName");
  }

  @Test
  public void shouldApplyVariablesFilterAndNotRetrieveVariables() throws Exception {
    // given
    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .variables()
        .handler((externalTask, externalTaskService) -> {
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertVariablesFilterAppliedAccordingToFetchAndLockPayload(objectMapper);
  }

  @Test
  public void shouldNotApplyVariablesFilter() throws Exception {
    // given
    ObjectMapper objectMapper = spy(ObjectMapper.class);
    whenNew(ObjectMapper.class).withNoArguments().thenReturn(objectMapper);

    mockFetchAndLockResponse(Collections.singletonList(MockProvider.createExternalTaskWithoutVariables()));

    ExternalTaskClient client = ExternalTaskClient.create()
      .baseUrl(MockProvider.BASE_URL)
      .build();

    final AtomicBoolean handlerInvoked = new AtomicBoolean(false);
    TopicSubscriptionBuilder topicSubscriptionBuilder =
      client.subscribe(MockProvider.TOPIC_NAME)
        .lockDuration(5000)
        .handler((externalTask, externalTaskService) -> {
          handlerInvoked.set(true);
        });

    // when
    topicSubscriptionBuilder.open();
    while (!handlerInvoked.get()) {
      // busy waiting
    }
    client.stop();

    // then
    assertVariablesFilterAppliedAccordingToFetchAndLockPayload(objectMapper, (String[]) null);
  }

  // helper //////////////////////////////////

  protected void assertVariablesFilterAppliedAccordingToFetchAndLockPayload(ObjectMapper objectMapper, String... expectedVariables) throws JsonProcessingException {
    ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
    verify(objectMapper, atLeastOnce()).writeValueAsBytes(payloads.capture());

    FetchAndLockRequestDto fetchAndLockRequestDto = (FetchAndLockRequestDto) payloads.getAllValues().stream()
      .filter(payload -> payload instanceof FetchAndLockRequestDto)
      .findFirst()
      .orElse(null);

    if (expectedVariables == null) {
      assertThat(fetchAndLockRequestDto.getTopics().get(0).getVariables()).isNull();
    } else if (expectedVariables.length > 0) {
      assertThat(fetchAndLockRequestDto.getTopics().get(0).getVariables()).containsExactly(expectedVariables);
    } else {
      assertThat(fetchAndLockRequestDto.getTopics().get(0).getVariables()).isEmpty();
    }
  }

  protected String encodeObjectToBase64(Object object) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
      objectOutputStream.writeObject(object);
    }

    return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
  }

  protected void assertObjectValue(ObjectValue objectValue, Class<?> type, String serializationDataFormat) {
    assertThat(objectValue.getType().getName()).isEqualTo("object");
    assertThat(objectValue.getObjectType()).isEqualTo(type);
    assertThat(objectValue.getObjectTypeName()).isEqualTo(type.getName());
    assertThat(objectValue.getSerializationDataFormat()).isEqualTo(serializationDataFormat);
  }

  protected void assertSpinVariableValue(ExternalTask externalTask, String variableName, String variableValue, String variableType) {
    assertThat(externalTask.getVariableTyped(variableName).getType().getName()).isEqualTo(variableType.toLowerCase());
    assertThat(externalTask.getVariableTyped(variableName).getValue().toString()).isEqualTo(variableValue);

    assertThat(externalTask.getAllVariablesTyped().getValueTyped(variableName).getType().getName()).isEqualTo(variableType.toLowerCase());
    assertThat(externalTask.getAllVariablesTyped().getValueTyped(variableName).getValue().toString()).isEqualTo(variableValue);

    assertThat((Object) externalTask.getVariable(variableName).toString()).isEqualTo(variableValue);

    assertThat(externalTask.getAllVariables().get(variableName).toString()).isEqualTo(variableValue);

    if (externalTask.getVariableTyped(variableName).getValue() instanceof JacksonJsonNode) {
      assertThat(externalTask.getVariableTyped(variableName).getValue()).isInstanceOf(JacksonJsonNode.class);
      assertThat(externalTask.getAllVariablesTyped().getValueTyped(variableName).getValue()).isInstanceOf(JacksonJsonNode.class);
      assertThat((Object) externalTask.getVariable(variableName)).isInstanceOf(JacksonJsonNode.class);
      assertThat(externalTask.getAllVariables().get(variableName)).isInstanceOf(JacksonJsonNode.class);
    }

    if (externalTask.getVariableTyped(variableName).getValue() instanceof DomXmlElement) {
      assertThat(externalTask.getVariableTyped(variableName).getValue()).isInstanceOf(DomXmlElement.class);
      assertThat(externalTask.getAllVariablesTyped().getValueTyped(variableName).getValue()).isInstanceOf(DomXmlElement.class);
      assertThat((Object) externalTask.getVariable(variableName)).isInstanceOf(DomXmlElement.class);
      assertThat(externalTask.getAllVariables().get(variableName)).isInstanceOf(DomXmlElement.class);
    }
  }

  protected TypedValueDto createTypedValueDto(Object variableValue, String variableType, boolean isTransient) {
    return createTypedValueDto(variableValue, variableType, isTransient, null, null);
  }

  protected TypedValueDto createTypedValueDto(Object variableValue, String variableType, String objectTypeName, String serializationDataFormat) {
    return createTypedValueDto(variableValue, variableType, null, objectTypeName, serializationDataFormat);
  }

  protected TypedValueDto createTypedValueDto(Object variableValue, String variableType) {
    return createTypedValueDto(variableValue, variableType, null, null, null);
  }

  protected TypedValueDto createTypedValueDto(Object variableValue, String variableType, Boolean isTransient, String objectTypeName, String serializationDataFormat) {
    TypedValueDto typedValueDto = new TypedValueDto();
    typedValueDto.setValue(variableValue);
    typedValueDto.setType(variableType);

    Map<String, Object> valueInfo = new HashMap<>();

    if (isTransient != null) {
      valueInfo.put("transient", isTransient);
    }

    if (objectTypeName != null) {
      valueInfo.put("objectTypeName", objectTypeName);
      valueInfo.put("serializationDataFormat", serializationDataFormat);
    }
    if (valueInfo.size() > 0) {
      typedValueDto.setValueInfo(valueInfo);
    }

    return typedValueDto;
  }

  protected void mockFetchAndLockResponse(List<ExternalTask> externalTasks) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] externalTasksAsBytes = objectMapper.writeValueAsBytes(externalTasks);
    HttpEntity entity = new ByteArrayEntity(externalTasksAsBytes);
    doReturn(entity)
      .when(closeableHttpResponse).getEntity();
  }

  protected void assertAllVariablesUntyped(ExternalTask externalTask) {
    assertThat(externalTask.getAllVariables().size()).isEqualTo(MockProvider.VARIABLES.size());

    MockProvider.VARIABLES.forEach((variableName, variableValue) -> {
      assertThat(variableName).isNotNull();
      assertThat(variableValue).isNotNull();

      if (variableValue.getType().equals("Date")) {
        assertThat(externalTask.getAllVariables().get(variableName)).isEqualTo(MockProvider.DATE_VARIABLE_VALUE);
      }
      else if (variableValue.getType().equals("Bytes")) {
        assertThat(externalTask.getAllVariables().get(variableName)).isEqualTo(MockProvider.BYTES_VARIABLE_VALUE);
      }
      else {
        assertThat(externalTask.getAllVariables().get(variableName)).isEqualTo(variableValue.getValue());
      }
    });
  }

  protected void assertAllVariablesTyped(ExternalTask externalTask) {
    assertThat(externalTask.getAllVariables().size()).isEqualTo(MockProvider.VARIABLES.size());

    MockProvider.VARIABLES.forEach((expectedVariableName, expectedVariableValue) -> {
      assertThat(expectedVariableName).isNotNull();
      assertThat(expectedVariableValue).isNotNull();

      TypedValue typedValue = externalTask.getAllVariablesTyped().getValueTyped(expectedVariableName);

      if (typedValue.getType().getName().equals("date")) {
        assertThat(typedValue.getValue()).isEqualTo(MockProvider.DATE_VARIABLE_VALUE);
      }
      else if (typedValue.getType().getName().equals("bytes")) {
        assertThat(typedValue.getValue()).isEqualTo(MockProvider.BYTES_VARIABLE_VALUE);
      }
      else {
        assertThat(typedValue.getType().getName()).isEqualTo(expectedVariableValue.getType().toLowerCase());
        assertThat(typedValue.getValue()).isEqualTo(expectedVariableValue.getValue());
      }
    });
  }

  protected void assertSingleVariableUntyped(ExternalTask externalTask) {
    boolean booleanValue = externalTask.getVariable(MockProvider.BOOLEAN_VARIABLE_NAME);
    assertThat(booleanValue).isEqualTo(MockProvider.BOOLEAN_VARIABLE_VALUE);

    short shortValue = externalTask.getVariable(MockProvider.SHORT_VARIABLE_NAME);
    assertThat(shortValue).isEqualTo(MockProvider.SHORT_VARIABLE_VALUE);

    int integerValue = externalTask.getVariable(MockProvider.INTEGER_VARIABLE_NAME);
    assertThat(integerValue).isEqualTo(MockProvider.INTEGER_VARIABLE_VALUE);

    long longValue = externalTask.getVariable(MockProvider.LONG_VARIABLE_NAME);
    assertThat(longValue).isEqualTo(MockProvider.LONG_VARIABLE_VALUE);

    double doubleValue = externalTask.getVariable(MockProvider.DOUBLE_VARIABLE_NAME);
    assertThat(doubleValue).isEqualTo(MockProvider.DOUBLE_VARIABLE_VALUE);

    String stringValue = externalTask.getVariable(MockProvider.STRING_VARIABLE_NAME);
    assertThat(stringValue).isEqualTo(MockProvider.STRING_VARIABLE_VALUE);

    Date dateValue = externalTask.getVariable(MockProvider.DATE_VARIABLE_NAME);
    assertThat(dateValue).isEqualTo(MockProvider.DATE_VARIABLE_VALUE);

    byte[] bytesValue = externalTask.getVariable(MockProvider.BYTES_VARIABLE_NAME);
    assertThat(bytesValue).isEqualTo(MockProvider.BYTES_VARIABLE_VALUE);

    Object nullValue = externalTask.getVariable(MockProvider.NULL_VARIABLE_NAME);
    assertThat(nullValue).isNull();
  }

  protected void assertSingleVariableTyped(ExternalTask externalTask) {
    BooleanValue booleanValue = externalTask.getVariableTyped(MockProvider.BOOLEAN_VARIABLE_NAME);
    assertThat(booleanValue.getType()).isEqualTo(PrimitiveValueType.BOOLEAN);
    assertThat(booleanValue.getValue()).isEqualTo(MockProvider.BOOLEAN_VARIABLE_VALUE);

    ShortValue shortValue = externalTask.getVariableTyped(MockProvider.SHORT_VARIABLE_NAME);
    assertThat(shortValue.getType()).isEqualTo(PrimitiveValueType.SHORT);
    assertThat(shortValue.getValue()).isEqualTo(MockProvider.SHORT_VARIABLE_VALUE);

    IntegerValue integerValue = externalTask.getVariableTyped(MockProvider.INTEGER_VARIABLE_NAME);
    assertThat(integerValue.getType()).isEqualTo(PrimitiveValueType.INTEGER);
    assertThat(integerValue.getValue()).isEqualTo(MockProvider.INTEGER_VARIABLE_VALUE);

    LongValue longValue = externalTask.getVariableTyped(MockProvider.LONG_VARIABLE_NAME);
    assertThat(longValue.getType()).isEqualTo(PrimitiveValueType.LONG);
    assertThat(longValue.getValue()).isEqualTo(MockProvider.LONG_VARIABLE_VALUE);

    DoubleValue doubleValue = externalTask.getVariableTyped(MockProvider.DOUBLE_VARIABLE_NAME);
    assertThat(doubleValue.getType()).isEqualTo(PrimitiveValueType.DOUBLE);
    assertThat(doubleValue.getValue()).isEqualTo(MockProvider.DOUBLE_VARIABLE_VALUE);

    StringValue stringValue = externalTask.getVariableTyped(MockProvider.STRING_VARIABLE_NAME);
    assertThat(stringValue.getType()).isEqualTo(PrimitiveValueType.STRING);
    assertThat(stringValue.getValue()).isEqualTo(MockProvider.STRING_VARIABLE_VALUE);

    DateValue dateValue = externalTask.getVariableTyped(MockProvider.DATE_VARIABLE_NAME);
    assertThat(dateValue.getType()).isEqualTo(PrimitiveValueType.DATE);
    assertThat(dateValue.getValue()).isEqualTo(MockProvider.DATE_VARIABLE_VALUE);

    BytesValue bytesValue = externalTask.getVariableTyped(MockProvider.BYTES_VARIABLE_NAME);
    assertThat(bytesValue.getType()).isEqualTo(PrimitiveValueType.BYTES);
    assertThat(bytesValue.getValue()).isEqualTo(MockProvider.BYTES_VARIABLE_VALUE);

    NullValueImpl nullValue = externalTask.getVariableTyped(MockProvider.NULL_VARIABLE_NAME);
    assertThat(nullValue.getType()).isEqualTo(PrimitiveValueType.NULL);
    assertThat(nullValue.getValue()).isNull();
  }

  protected void assertCompleteRequestSerialization(ObjectMapper objectMapper) throws JsonProcessingException {
    ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
    verify(objectMapper, atLeastOnce()).writeValueAsBytes(payloads.capture());

    boolean isAsserted = false;
    for (Object request : payloads.getAllValues()) {
      if (request instanceof CompleteRequestDto) {
        CompleteRequestDto completeRequestDto = (CompleteRequestDto) request;
        Map<String, TypedValueDto> typedValueDtoMap = completeRequestDto.getVariables();

        TypedValueDto booleanValueDto = typedValueDtoMap.get(MockProvider.BOOLEAN_VARIABLE_NAME);
        assertThat(booleanValueDto.getType()).isEqualTo(MockProvider.BOOLEAN_VARIABLE_TYPE);
        assertThat(booleanValueDto.getValue()).isEqualTo(MockProvider.BOOLEAN_VARIABLE_VALUE);

        TypedValueDto shortValueDto = typedValueDtoMap.get(MockProvider.SHORT_VARIABLE_NAME);
        assertThat(shortValueDto.getType()).isEqualTo(MockProvider.SHORT_VARIABLE_TYPE);
        assertThat(shortValueDto.getValue()).isEqualTo(MockProvider.SHORT_VARIABLE_VALUE);

        TypedValueDto integerValueDto = typedValueDtoMap.get(MockProvider.INTEGER_VARIABLE_NAME);
        assertThat(integerValueDto.getType()).isEqualTo(MockProvider.INTEGER_VARIABLE_TYPE);
        assertThat(integerValueDto.getValue()).isEqualTo(MockProvider.INTEGER_VARIABLE_VALUE);

        TypedValueDto longValueDto = typedValueDtoMap.get(MockProvider.LONG_VARIABLE_NAME);
        assertThat(longValueDto.getType()).isEqualTo(MockProvider.LONG_VARIABLE_TYPE);
        assertThat(longValueDto.getValue()).isEqualTo(MockProvider.LONG_VARIABLE_VALUE);

        TypedValueDto doubleValueDto = typedValueDtoMap.get(MockProvider.DOUBLE_VARIABLE_NAME);
        assertThat(doubleValueDto.getType()).isEqualTo(MockProvider.DOUBLE_VARIABLE_TYPE);
        assertThat(doubleValueDto.getValue()).isEqualTo(MockProvider.DOUBLE_VARIABLE_VALUE);

        TypedValueDto stringValueDto = typedValueDtoMap.get(MockProvider.STRING_VARIABLE_NAME);
        assertThat(stringValueDto.getType()).isEqualTo(MockProvider.STRING_VARIABLE_TYPE);
        assertThat(stringValueDto.getValue()).isEqualTo(MockProvider.STRING_VARIABLE_VALUE);

        TypedValueDto dateValueDto = typedValueDtoMap.get(MockProvider.DATE_VARIABLE_NAME);
        assertThat(dateValueDto.getType()).isEqualTo(MockProvider.DATE_VARIABLE_TYPE);
        assertThat(dateValueDto.getValue()).isEqualTo(MockProvider.DATE_VARIABLE_VALUE_SERIALIZED);

        TypedValueDto bytesValue = typedValueDtoMap.get(MockProvider.BYTES_VARIABLE_NAME);
        assertThat(bytesValue.getType()).isEqualTo(MockProvider.BYTES_VARIABLE_TYPE);
        assertThat(bytesValue.getValue()).isEqualTo(MockProvider.BYTES_VARIABLE_VALUE_SERIALIZED);

        TypedValueDto nullValueDto = typedValueDtoMap.get(MockProvider.NULL_VARIABLE_NAME);
        assertThat(nullValueDto.getType()).isEqualTo(MockProvider.NULL_VARIABLE_TYPE);
        assertThat(nullValueDto.getValue()).isNull();

        isAsserted = true;
      }
    }

    assertThat(isAsserted).isTrue();
  }

  protected void assertVariablePayloadOfCompleteRequest(ObjectMapper objectMapper, Map<String, TypedValueDto> expectedDtoMap) throws JsonProcessingException {
    assertVariablePayloadOfCompleteRequest(objectMapper, expectedDtoMap, false);
  }

  protected void assertLocalVariablePayloadOfCompleteRequest(ObjectMapper objectMapper, Map<String, TypedValueDto> expectedDtoMap) throws JsonProcessingException {
    assertVariablePayloadOfCompleteRequest(objectMapper, expectedDtoMap, true);
  }

  protected void assertVariablePayloadOfCompleteRequest(ObjectMapper objectMapper, Map<String, TypedValueDto> expectedDtoMap, boolean assertLocalVariables) throws JsonProcessingException {
    ArgumentCaptor<Object> payloads = ArgumentCaptor.forClass(Object.class);
    verify(objectMapper, atLeastOnce()).writeValueAsBytes(payloads.capture());

    final Boolean[] isAsserted = {false};

    CompleteRequestDto completeRequestDto = (CompleteRequestDto) payloads.getAllValues().stream()
      .filter(payload -> payload instanceof CompleteRequestDto)
      .findFirst()
      .orElse(null);

    Map<String, TypedValueDto> variableMap;
    if (assertLocalVariables) {
      variableMap = completeRequestDto.getLocalVariables();
    } else {
      variableMap = completeRequestDto.getVariables();
    }

    assertThat(expectedDtoMap.size()).isEqualTo(variableMap.size());

    expectedDtoMap.forEach((variableName, typedValueDto) -> {
      assertThat(variableMap.get(variableName).getType()).isEqualTo(typedValueDto.getType());
      assertThat(variableMap.get(variableName).getValue()).isEqualTo(typedValueDto.getValue());

      if (typedValueDto.getValueInfo() != null && typedValueDto.getValueInfo().get("transient") != null) {
        boolean expectedTransience = (boolean) typedValueDto.getValueInfo().get("transient");
        if (expectedTransience) {
          assertThat((boolean) variableMap.get(variableName).getValueInfo().get("transient")).isTrue();
        } else {
          assertThat(variableMap.get(variableName).getValueInfo().get("transient")).isNull();
        }
      }

      if (typedValueDto.getValueInfo() != null && typedValueDto.getValueInfo().get("objectTypeName") != null) {
        assertThat(variableMap.get(variableName).getValueInfo().get("objectTypeName")).isEqualTo(typedValueDto.getValueInfo().get("objectTypeName"));
      }

      if (typedValueDto.getValueInfo() != null && typedValueDto.getValueInfo().get("serializationDataFormat") != null) {
        assertThat(variableMap.get(variableName).getValueInfo().get("serializationDataFormat"))
          .isEqualTo(typedValueDto.getValueInfo().get("serializationDataFormat"));
      }

      isAsserted[0] = true;
    });

    assertThat(isAsserted[0]).isTrue();
  }

}

