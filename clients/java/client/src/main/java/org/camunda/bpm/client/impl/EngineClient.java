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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.impl.variable.VariableMappers;
import org.camunda.bpm.client.interceptor.impl.RequestInterceptorHandler;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.task.impl.dto.BpmnErrorRequestDto;
import org.camunda.bpm.client.task.impl.dto.CompleteRequestDto;
import org.camunda.bpm.client.task.impl.dto.ExtendLockRequestDto;
import org.camunda.bpm.client.task.impl.dto.FailureRequestDto;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.client.topic.impl.dto.FetchAndLockRequestDto;
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class EngineClient {

  protected static final String EXTERNAL_TASK_RESOURCE_PATH = "/external-task";
  protected static final String FETCH_AND_LOCK_RESOURCE_PATH = EXTERNAL_TASK_RESOURCE_PATH + "/fetchAndLock";
  public static final String ID_PATH_PARAM = "{id}";
  protected static final String ID_RESOURCE_PATH = EXTERNAL_TASK_RESOURCE_PATH + "/" + ID_PATH_PARAM;
  public static final String UNLOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/unlock";
  public static final String COMPLETE_RESOURCE_PATH = ID_RESOURCE_PATH + "/complete";
  public static final String FAILURE_RESOURCE_PATH = ID_RESOURCE_PATH + "/failure";
  public static final String BPMN_ERROR_RESOURCE_PATH = ID_RESOURCE_PATH + "/bpmnError";
  public static final String EXTEND_LOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/extendLock";

  protected String baseUrl;
  protected String workerId;
  protected int maxTasks;
  protected RequestExecutor engineInteraction;

  protected VariableMappers variableMappers;

  public EngineClient(String workerId, int maxTasks, String baseUrl, RequestInterceptorHandler requestInterceptorHandler, VariableMappers variableMappers, ObjectMapper objectMapper) {
    this.workerId = workerId;
    this.maxTasks = maxTasks;
    this.engineInteraction = new RequestExecutor(requestInterceptorHandler, objectMapper);
    this.baseUrl = engineInteraction.sanitizeUrl(baseUrl);
    this.variableMappers = variableMappers;
  }

  public List<ExternalTask> fetchAndLock(List<TopicRequestDto> topics) throws EngineClientException {
    FetchAndLockRequestDto payload = new FetchAndLockRequestDto(workerId, maxTasks, topics);
    String resourceUrl = baseUrl + FETCH_AND_LOCK_RESOURCE_PATH;
    ExternalTask[] externalTasks = engineInteraction.postRequest(resourceUrl, payload, ExternalTaskImpl[].class);
    return Arrays.asList(externalTasks);
  }

  public void unlock(String taskId) throws EngineClientException {
    String resourcePath = UNLOCK_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = baseUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, null, Void.class);
  }

  public void complete(String taskId, Map<String, Object> variables, Map<String, Object> localVariables) throws EngineClientException {
    Map<String, TypedValueDto> typedValueDtoMap = new HashMap<>();
    if (variables != null) {
      typedValueDtoMap.putAll(variableMappers.serializeVariables(variables));
    }

    Map<String, TypedValueDto> localTypedValueDtoMap = new HashMap<>();
    if (localVariables != null) {
      localTypedValueDtoMap.putAll(variableMappers.serializeVariables(localVariables));
    }

    CompleteRequestDto payload = new CompleteRequestDto(workerId, typedValueDtoMap, localTypedValueDtoMap);
    String resourcePath = COMPLETE_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = baseUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void failure(String taskId, String errorMessage, String errorDetails, int retries, long retryTimeout) throws EngineClientException {
    FailureRequestDto payload = new FailureRequestDto(workerId, errorMessage, errorDetails, retries, retryTimeout);
    String resourcePath = FAILURE_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = baseUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void bpmnError(String taskId, String errorCode) throws EngineClientException {
    BpmnErrorRequestDto payload = new BpmnErrorRequestDto(workerId, errorCode);
    String resourcePath = BPMN_ERROR_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = baseUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void extendLock(String taskId, long newDuration) throws EngineClientException {
    ExtendLockRequestDto payload = new ExtendLockRequestDto(workerId, newDuration);
    String resourcePath = EXTEND_LOCK_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = baseUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getWorkerId() {
    return workerId;
  }

}
