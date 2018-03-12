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

import org.camunda.bpm.client.interceptor.impl.RequestInterceptorHandler;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.task.impl.FailureRequestDto;
import org.camunda.bpm.client.task.impl.dto.BpmnErrorRequestDto;
import org.camunda.bpm.client.task.impl.dto.CompleteRequestDto;
import org.camunda.bpm.client.task.impl.dto.ExtendLockRequestDto;
import org.camunda.bpm.client.topic.impl.dto.FetchAndLockRequestDto;
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class EngineClient {

  private static final int MAX_TASKS = 10;
  private static final String EXTERNAL_TASK_RESOURCE_PATH = "/external-task";
  public static final String FETCH_AND_LOCK_RESOURCE_PATH = EXTERNAL_TASK_RESOURCE_PATH + "/fetchAndLock";
  public static final String ID_PATH_PARAM = "{id}";
  private static final String ID_RESOURCE_PATH = EXTERNAL_TASK_RESOURCE_PATH + "/" + ID_PATH_PARAM;
  public static final String UNLOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/unlock";
  public static final String COMPLETE_RESOURCE_PATH = ID_RESOURCE_PATH + "/complete";
  public static final String FAILURE_RESOURCE_PATH = ID_RESOURCE_PATH + "/failure";
  public static final String BPMN_ERROR_RESOURCE_PATH = ID_RESOURCE_PATH + "/bpmnError";
  public static final String EXTEND_LOCK_RESOURCE_PATH = ID_RESOURCE_PATH + "/extendLock";

  private String endpointUrl;
  private String workerId;
  private RequestExecutor engineInteraction;

  public EngineClient(String workerId, String endpointUrl, RequestInterceptorHandler requestInterceptorHandler) {
    this.workerId = workerId;
    this.engineInteraction = new RequestExecutor(requestInterceptorHandler);
    this.endpointUrl = engineInteraction.sanitizeUrl(endpointUrl);
  }

  public List<ExternalTask> fetchAndLock(List<TopicRequestDto> topics) {
    FetchAndLockRequestDto payload = new FetchAndLockRequestDto(workerId, MAX_TASKS, topics);
    String resourceUrl = endpointUrl + FETCH_AND_LOCK_RESOURCE_PATH;
    ExternalTask[] lockedTasksResponse = engineInteraction.postRequest(resourceUrl, payload, ExternalTaskImpl[].class);
    return Arrays.asList(lockedTasksResponse);
  }

  public void unlock(String taskId) {
    String resourcePath = UNLOCK_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = endpointUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, null, Void.class);
  }

  public void complete(String taskId) {
    CompleteRequestDto payload = new CompleteRequestDto(workerId);
    String resourcePath = COMPLETE_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = endpointUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void failure(String taskId, String errorMessage, String errorDetails, int retries, long retryTimeout) {
    FailureRequestDto payload = new FailureRequestDto(workerId, errorMessage, errorDetails, retries, retryTimeout);
    String resourcePath = FAILURE_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = endpointUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void bpmnError(String taskId, String errorCode) {
    BpmnErrorRequestDto payload = new BpmnErrorRequestDto(workerId, errorCode);
    String resourcePath = BPMN_ERROR_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = endpointUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public void extendLock(String taskId, long newDuration) {
    ExtendLockRequestDto payload = new ExtendLockRequestDto(workerId, newDuration);
    String resourcePath = EXTEND_LOCK_RESOURCE_PATH.replace("{id}", taskId);
    String resourceUrl = endpointUrl + resourcePath;
    engineInteraction.postRequest(resourceUrl, payload, Void.class);
  }

  public String getEndpointUrl() {
    return endpointUrl;
  }

  public String getWorkerId() {
    return workerId;
  }

}
