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
package org.camunda.bpm.client.rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.util.PropertyUtil.DEFAULT_PROPERTIES_PATH;
import static org.camunda.bpm.client.util.PropertyUtil.loadProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.camunda.bpm.client.dto.HistoricProcessInstanceDto;
import org.camunda.bpm.client.dto.IncidentDto;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.dto.TaskDto;
import org.camunda.bpm.client.dto.VariableInstanceDto;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.rules.ExternalResource;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class EngineRule extends ExternalResource {

  protected static final String URI_DEPLOYMEN_CREATE = "%s/deployment/create";
  protected static final String URI_DEPLOYMENT_DELETE = "%s/deployment/%s";
  protected static final String URI_START_PROCESS_INSTANCE = "%s/process-definition/%s/start";
  protected static final String URI_START_PROCESS_INSTANCE_TENANT = "%s/process-definition/key/%s/tenant-id/%s/start";
  protected static final String URI_START_PROCESS_INSTANCE_BY_KEY = "%s/process-definition/key/%s/start";
  protected static final String URI_GET_TASKS = "%s/task";
  protected static final String URI_GET_INCIDENTS = "%s/incident";
  protected static final String URI_GET_EXTERNAL_TASKS = "%s/external-task";
  protected static final String URI_GET_HISTORIC_PROCESS_INSTANCE = "%s/history/process-instance";
  protected static final String URI_DELETE_PROCESS_INSTANCE = "%s/process-instance/%s?skipCustomListeners=true";
  protected static final String URI_GET_VARIABLE_INSTANCE = "%s/variable-instance";

  protected Properties properties;
  protected CloseableHttpClient httpClient;
  protected ObjectMapper objectMapper;

  protected Set<String> deployments = new HashSet<>();

  public EngineRule() {
    this(DEFAULT_PROPERTIES_PATH);
  }

  public EngineRule(String propertiesLocation) {
    this(() -> loadProperties(propertiesLocation));
  }

  public EngineRule(Supplier<Properties> properties) {
    this.properties = properties.get();
  }

  @Override
  protected void before() throws Throwable {
    deployments.clear();

    initializeHttpClient();
    initializeObjectMapper();
  }

  @Override
  protected void after() {
    cleanEngine();
  }

  protected void initializeHttpClient() {
    if (httpClient == null) {
      httpClient = HttpClientBuilder.create().build();
    }
  }

  protected void initializeObjectMapper() {
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
      objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
  }

  protected void cleanEngine() {
    Map<String, Boolean> parameters = new HashMap<>();
    parameters.put("cascade", true);
    parameters.put("skipCustomListeners", true);
    parameters.put("skipIoMappings", true);

    deployments.forEach((deployment) -> {
      deleteDeployment(deployment, parameters);
    });
  }

  public List<ProcessDefinitionDto> deploy(BpmnModelInstance... processes) {
    return deploy(null, processes);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public List<ProcessDefinitionDto> deploy(String tenantId, BpmnModelInstance... processes) {
    List<ProcessDefinitionDto> definitions = new ArrayList<>();

    HttpPost post = createDeploymentRequest(tenantId, processes);
    HashMap<String, Object> response = executeRequest(post, HashMap.class);

    deployments.add((String) response.get("id"));

    Map<String, Map> definitionMap = (Map<String, Map>) response.get("deployedProcessDefinitions");

    definitionMap.forEach((key, value) -> {
      try {
        String definitionAsString = objectMapper.writeValueAsString(value);
        ProcessDefinitionDto definition = objectMapper.readValue(definitionAsString, ProcessDefinitionDto.class);
        definitions.add(definition);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    return definitions;
  }

  protected HttpPost createDeploymentRequest(BpmnModelInstance... processes) {
    return createDeploymentRequest(null, processes);
  }

  protected HttpPost createDeploymentRequest(String tenantId, BpmnModelInstance... processes) {
    String uri = String.format(URI_DEPLOYMEN_CREATE, getEngineUrl());
    HttpPost post = new HttpPost(uri);

    MultipartEntityBuilder builder = MultipartEntityBuilder.create()
      .addTextBody("deployment-name", "deployment")
      .addTextBody("enable-duplicate-filtering", "false")
      .addTextBody("deployment-source", "process application");

    if (tenantId != null) {
      builder.addTextBody("tenant-id", tenantId);
    }

    for (int i = 0; i < processes.length; i++) {
      BpmnModelInstance process = processes[i];
      String processAsString = Bpmn.convertToString(process);
      builder.addBinaryBody(
          String.format("data %d", i),
          processAsString.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_OCTET_STREAM,
          String.format("test%d.bpmn", i));
    }

    HttpEntity entity = builder.build();
    post.setEntity(entity);
    return post;
  }

  public void deleteDeployment(String deploymentId, Map<String, Boolean> parameters) {
    String endpoint = String.format(URI_DEPLOYMENT_DELETE, getEngineUrl(), deploymentId);

    List<String> queryParameters = new ArrayList<>();
    parameters.forEach((key, value) -> {
      queryParameters.add(key + "=" + value);
    });

    if (!queryParameters.isEmpty()) {
      endpoint = endpoint + "?" + String.join("&", queryParameters);
    }

    HttpDelete httpDelete = new HttpDelete(endpoint);
    executeRequest(httpDelete, Void.class);
  }

  public ProcessInstanceDto startProcessInstance(String processDefinitionId) {
    return startProcessInstance(processDefinitionId, null, (Map<String, TypedValue>) null);
  }

  public ProcessInstanceDto startProcessInstance(String processDefinitionId, String businessKey) {
    return startProcessInstance(processDefinitionId, businessKey, (Map<String, TypedValue>) null);
  }

  public ProcessInstanceDto startProcessInstance(String processDefinitionId, String businessKey, String variableName, TypedValue variableValue) {
    Map<String, TypedValue> variables = new HashMap<>();
    variables.put(variableName, variableValue);
    return startProcessInstance(processDefinitionId, businessKey, variables);
  }

  public ProcessInstanceDto startProcessInstance(String processDefinitionId, String variableName, TypedValue variableValue) {
    return startProcessInstance(processDefinitionId, null, variableName, variableValue);
  }

  public ProcessInstanceDto startProcessInstance(String processDefinitionId, Map<String, TypedValue> variables) {
    return startProcessInstance(processDefinitionId, null, variables);
  }

  public ProcessInstanceDto startProcessInstance(String processDefinitionId, String businessKey, Map<String, TypedValue> variables) {
    return startProcessInstance(processDefinitionId, businessKey, variables, null, null);
  }

  public ProcessInstanceDto startProcessInstanceByKey(String processDefinitionKey) {
    return startProcessInstance(null, null, null, processDefinitionKey, null);
  }

  public ProcessInstanceDto startProcessInstanceByKey(String processDefinitionKey, String tenantId) {
    return startProcessInstance(null, null, null, processDefinitionKey, tenantId);
  }

  public ProcessInstanceDto startProcessInstance(String processDefinitionId, String businessKey, Map<String, TypedValue> variables, String processDefinitionKey, String tenantId) {
    if (variables == null) {
      variables = new HashMap<>();
    }

    try {
      String uri = null;
      if (tenantId != null && processDefinitionKey != null) {
        uri = String.format(URI_START_PROCESS_INSTANCE_TENANT, getEngineUrl(), processDefinitionKey, tenantId);
      } else if (processDefinitionKey != null) {
        uri = String.format(URI_START_PROCESS_INSTANCE_BY_KEY, getEngineUrl(), processDefinitionKey);
      } else {
        uri = String.format(URI_START_PROCESS_INSTANCE, getEngineUrl(), processDefinitionId);
      }

      HttpPost httpPost = new HttpPost(uri);
      httpPost.addHeader("Content-Type", "application/json");

      Map<String, TypedValueField> map = new HashMap<>();

      variables.forEach((key, typedValue) -> {
        TypedValueField dto = new TypedValueField();

        ValueType type = typedValue.getType();
        if (type != null) {
          String typeName = type.getName();
          dto.setType(toRestApiTypeName(typeName));
          dto.setValueInfo(type.getValueInfo(typedValue));
        }

        if(typedValue instanceof SerializableValue) {
          SerializableValue serializableValue = (SerializableValue) typedValue;

          if(serializableValue.isDeserialized()) {
            dto.setValue(serializableValue.getValue());
          }
          else {
            dto.setValue(serializableValue.getValueSerialized());
          }

        }
        else if (typedValue instanceof FileValueImpl) {
          dto.setValue(((FileValueImpl)typedValue).getByteArray());
        }
        else {
          dto.setValue(typedValue.getValue());
        }

        map.put(key, dto);
      });

      StringBuilder payload = new StringBuilder();
      payload.append("{");
      if (!map.isEmpty()) {
        String variablesAsString = objectMapper.writeValueAsString(map);
        payload.append("\"variables\": " + variablesAsString);
        payload.append(",");
      }
      if (businessKey != null) {
        payload.append("\"businessKey\": \"" + businessKey + "\"");
      } else {
        payload.append("\"businessKey\": null");
      }
      payload.append("}");

      httpPost.setEntity(new StringEntity(payload.toString()));

      return executeRequest(httpPost, ProcessInstanceDto.class);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  public VariableInstanceDto getVariableByProcessInstanceId(String processInstanceId) {
    return getVariableByProcessInstanceId(processInstanceId, null);
  }

  public VariableInstanceDto getVariableByProcessInstanceId(String processInstanceId, String varName) {
    List<VariableInstanceDto> variables = getVariablesByProcessInstanceIdAndVariableName(processInstanceId, varName);
    assertThat(variables).hasSize(1);
    return (VariableInstanceDto) variables.get(0);
  }

  public List<VariableInstanceDto> getVariablesByProcessInstanceIdAndVariableName(String processInstanceId, String varName) {
    String uri = String.format(URI_GET_VARIABLE_INSTANCE, getEngineUrl()) + "?processInstanceIdIn=" + processInstanceId;

    if (varName != null) {
      uri = uri + "&variableName=" + varName;
    }

    HttpGet httpGet = new HttpGet(uri);
    VariableInstanceDto[] variables = executeRequest(httpGet, VariableInstanceDto[].class);
    return Arrays.asList(variables);
  }

  public TaskDto getTaskByProcessInstanceId(String processInstanceId) {
    String uri = String.format(URI_GET_TASKS, getEngineUrl()) + "?processInstanceId=" + processInstanceId;
    HttpGet httpGet = new HttpGet(uri);
    TaskDto[] tasks = executeRequest(httpGet, TaskDto[].class);
    assertThat(tasks).hasSize(1);
    return (TaskDto) tasks[0];
  }

  public IncidentDto getIncidentByProcessInstanceId(String processInstanceId) {
    String uri = String.format(URI_GET_INCIDENTS, getEngineUrl()) + "?processInstanceId=" + processInstanceId;
    HttpGet httpGet = new HttpGet(uri);
    IncidentDto[] incidents = executeRequest(httpGet, IncidentDto[].class);
    assertThat(incidents).hasSize(1);
    return (IncidentDto) incidents[0];
  }

  public ExternalTask getExternalTaskByProcessInstanceId(String processInstanceId) {
    List<ExternalTaskImpl> externalTasks = getExternalTasksByProcessInstanceId(processInstanceId);
    assertThat(externalTasks).hasSize(1);
    return externalTasks.get(0);
  }

  public List<ExternalTaskImpl> getExternalTasksByProcessInstanceId(String processInstanceId) {
    String uri = String.format(URI_GET_EXTERNAL_TASKS, getEngineUrl()) + "?processInstanceId=" + processInstanceId;
    HttpGet httpGet = new HttpGet(uri);
    return Arrays.asList(executeRequest(httpGet, ExternalTaskImpl[].class));
  }

  public HistoricProcessInstanceDto getHistoricProcessInstanceById(String processInstanceId) {
    String uri = String.format(URI_GET_HISTORIC_PROCESS_INSTANCE, getEngineUrl()) + "/" + processInstanceId;
    HttpGet httpGet = new HttpGet(uri);
    HistoricProcessInstanceDto processInstance = executeRequest(httpGet, HistoricProcessInstanceDto.class);
    assertThat(processInstance).isNotNull();
    return processInstance;
  }

  public void deleteProcessInstance(String processInstanceId) {
    String uri = String.format(URI_DELETE_PROCESS_INSTANCE, getEngineUrl(), processInstanceId);
    HttpDelete httpDelete = new HttpDelete(uri);
    executeRequest(httpDelete, Void.class);
  }

  protected <T> T executeRequest(HttpUriRequest httpRequest, Class<T> responseDtoClass) {
    try {
      return httpClient.execute(httpRequest, handleResponse(responseDtoClass));
    } catch (ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected <T> HttpClientResponseHandler<T> handleResponse(final Class<T> responseDtoClass) {
    return new AbstractHttpClientResponseHandler<>() {
      @Override
      public T handleEntity(HttpEntity responseEntity) {
        T deserializedResponse = null;
        if (!responseDtoClass.isAssignableFrom(Void.class)) {
          try {
            deserializedResponse = deserializeResponse(responseEntity, responseDtoClass);
            EntityUtils.consume(responseEntity);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        return deserializedResponse;
      }
    };
  }

  protected <T> T deserializeResponse(HttpEntity httpEntity, Class<T> responseDtoClass) {
    try {
      InputStream responseBody = httpEntity.getContent();
      return objectMapper.readValue(responseBody, responseDtoClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public CloseableHttpClient getHttpClient() {
    return httpClient;
  }

  protected String getEngineUrl() {
    return properties.get("camunda.engine.rest").toString() +
        properties.get("camunda.engine.name").toString();
  }

  protected String toRestApiTypeName(String name) {
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

}
