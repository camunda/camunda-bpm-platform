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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.client.UrlResolver;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.OrderingConfig;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.task.impl.dto.BpmnErrorRequestDto;
import org.camunda.bpm.client.task.impl.dto.CompleteRequestDto;
import org.camunda.bpm.client.task.impl.dto.ExtendLockRequestDto;
import org.camunda.bpm.client.task.impl.dto.FailureRequestDto;
import org.camunda.bpm.client.task.impl.dto.LockRequestDto;
import org.camunda.bpm.client.task.impl.dto.SetVariablesRequestDto;
import org.camunda.bpm.client.topic.impl.dto.FetchAndLockRequestDto;
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.client.variable.impl.TypedValues;

/**
 * @author Tassilo Weidner
 */
public class EngineClient {

  protected static final String EXTERNAL_TASK_RESOURCE_PATH = "/external-task";
  protected static final String EXTERNAL_TASK__PROCESS_RESOURCE_PATH = "/process-instance";
  protected static final String FETCH_AND_LOCK_RESOURCE_PATH = EXTERNAL_TASK_RESOURCE_PATH + "/fetchAndLock";
  public static final String ID_PATH_PARAM = "{id}";
  protected static final String ID_RESOURCE_PATH = EXTERNAL_TASK_RESOURCE_PATH + "/" + ID_PATH_PARAM;
  public static final String LOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/lock";
  public static final String EXTEND_LOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/extendLock";
  public static final String SET_VARIABLES_RESOURCE_PATH =
      EXTERNAL_TASK__PROCESS_RESOURCE_PATH + "/" + ID_PATH_PARAM + "/variables";
  public static final String UNLOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/unlock";
  public static final String COMPLETE_RESOURCE_PATH = ID_RESOURCE_PATH + "/complete";
  public static final String FAILURE_RESOURCE_PATH = ID_RESOURCE_PATH + "/failure";
  public static final String BPMN_ERROR_RESOURCE_PATH = ID_RESOURCE_PATH + "/bpmnError";
  public static final String NAME_PATH_PARAM = "{name}";
  public static final String PROCESS_INSTANCE_RESOURCE_PATH = "/process-instance";
  public static final String PROCESS_INSTANCE_ID_RESOURCE_PATH = PROCESS_INSTANCE_RESOURCE_PATH + "/" + ID_PATH_PARAM;
  public static final String GET_BINARY_VARIABLE =
      PROCESS_INSTANCE_ID_RESOURCE_PATH + "/variables/" + NAME_PATH_PARAM + "/data";
  protected UrlResolver urlResolver;
  protected String workerId;
  protected int maxTasks;
  protected boolean usePriority;
  protected OrderingConfig orderingConfig;
  protected Long asyncResponseTimeout;
  protected RequestExecutor engineInteraction;
  protected TypedValues typedValues;

  public EngineClient(String workerId,
                      int maxTasks,
                      Long asyncResponseTimeout,
                      String baseUrl,
                      RequestExecutor engineInteraction) {
    this(workerId, maxTasks, asyncResponseTimeout, baseUrl, engineInteraction, true, OrderingConfig.empty());
  }

  public EngineClient(String workerId,
                      int maxTasks,
                      Long asyncResponseTimeout,
                      String baseUrl,
                      RequestExecutor engineInteraction,
                      boolean usePriority,
                      OrderingConfig orderingConfig) {
    this.workerId = workerId;
    this.asyncResponseTimeout = asyncResponseTimeout;
    this.maxTasks = maxTasks;
    this.usePriority = usePriority;
    this.engineInteraction = engineInteraction;
    this.urlResolver = new PermanentUrlResolver(baseUrl);
    this.orderingConfig = orderingConfig;
  }

  public EngineClient(String workerId,
                      int maxTasks,
                      Long asyncResponseTimeout,
                      UrlResolver urlResolver,
                      RequestExecutor engineInteraction) {
    this(workerId, maxTasks, asyncResponseTimeout, urlResolver, engineInteraction, true, OrderingConfig.empty());
  }

  public EngineClient(String workerId,
                      int maxTasks,
                      Long asyncResponseTimeout,
                      UrlResolver urlResolver,
                      RequestExecutor engineInteraction,
                      boolean usePriority,
                      OrderingConfig orderingConfig) {
    this.workerId = workerId;
    this.asyncResponseTimeout = asyncResponseTimeout;
    this.maxTasks = maxTasks;
    this.usePriority = usePriority;
    this.engineInteraction = engineInteraction;
    this.urlResolver = urlResolver;
    this.orderingConfig = orderingConfig;
  }

  public List<ExternalTask> fetchAndLock(List<TopicRequestDto> topics) {
    FetchAndLockRequestDto payload = new FetchAndLockRequestDto(workerId, maxTasks, asyncResponseTimeout, topics,
        usePriority, orderingConfig);

    String resourceUrl = getBaseUrl() + FETCH_AND_LOCK_RESOURCE_PATH;
    ExternalTask[] externalTasks = engineInteraction.postRequest(resourceUrl, payload, ExternalTaskImpl[].class);
    return Arrays.asList(externalTasks);
  }

  public void lock(String taskId, long lockDuration) {
    LockRequestDto payload = new LockRequestDto(workerId, lockDuration);
    String resourcePath = LOCK_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = getBaseUrl() + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void unlock(String taskId) {
    String resourcePath = UNLOCK_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = getBaseUrl() + resourcePath;
    engineInteraction.postRequest(resourceUrl, null, Void.class);
  }

  public void complete(String taskId, Map<String, Object> variables, Map<String, Object> localVariables) {
    Map<String, TypedValueField> typedValueDtoMap = typedValues.serializeVariables(variables);
    Map<String, TypedValueField> localTypedValueDtoMap = typedValues.serializeVariables(localVariables);

    CompleteRequestDto payload = new CompleteRequestDto(workerId, typedValueDtoMap, localTypedValueDtoMap);
    String resourcePath = COMPLETE_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = getBaseUrl() + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void setVariables(String processId, Map<String, Object> variables) {
    Map<String, TypedValueField> typedValueDtoMap = typedValues.serializeVariables(variables);
    SetVariablesRequestDto payload = new SetVariablesRequestDto(workerId, typedValueDtoMap);
    String resourcePath = SET_VARIABLES_RESOURCE_PATH.replace("{id}", processId);
    String resourceUrl = getBaseUrl() + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void failure(String taskId,
                      String errorMessage,
                      String errorDetails,
                      int retries,
                      long retryTimeout,
                      Map<String, Object> variables,
                      Map<String, Object> localVariables) {
    Map<String, TypedValueField> typedValueDtoMap = typedValues.serializeVariables(variables);
    Map<String, TypedValueField> localTypedValueDtoMap = typedValues.serializeVariables(localVariables);

    FailureRequestDto payload = new FailureRequestDto(workerId, errorMessage, errorDetails, retries, retryTimeout,
        typedValueDtoMap, localTypedValueDtoMap);
    String resourcePath = FAILURE_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = getBaseUrl() + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void bpmnError(String taskId, String errorCode, String errorMessage, Map<String, Object> variables) {
    Map<String, TypedValueField> typeValueDtoMap = typedValues.serializeVariables(variables);
    BpmnErrorRequestDto payload = new BpmnErrorRequestDto(workerId, errorCode, errorMessage, typeValueDtoMap);
    String resourcePath = BPMN_ERROR_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = getBaseUrl() + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void extendLock(String taskId, long newDuration)  {
    ExtendLockRequestDto payload = new ExtendLockRequestDto(workerId, newDuration);
    String resourcePath = EXTEND_LOCK_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = getBaseUrl() + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public byte[] getLocalBinaryVariable(String variableName, String executionId)  {
    String resourcePath =  getBaseUrl()  + GET_BINARY_VARIABLE
            .replace(ID_PATH_PARAM, executionId)
            .replace(NAME_PATH_PARAM, variableName);

    return engineInteraction.getRequest(resourcePath);
  }

  public String getBaseUrl() {
    return urlResolver.getBaseUrl();
  }

  public String getWorkerId() {
    return workerId;
  }

  public void setTypedValues(TypedValues typedValues) {
    this.typedValues = typedValues;
  }

  public boolean isUsePriority() {
    return usePriority;
  }
}
