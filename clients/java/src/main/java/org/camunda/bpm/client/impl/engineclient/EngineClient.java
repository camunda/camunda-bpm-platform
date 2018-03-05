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

import org.camunda.bpm.client.LockedTask;
import org.camunda.bpm.client.impl.dto.FetchAndLockRequestDto;
import org.camunda.bpm.client.impl.dto.LockedTaskDto;
import org.camunda.bpm.client.impl.dto.TaskTopicRequestDto;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tassilo Weidner
 */
public class EngineClient {

  private static final int MAX_TASKS = 10;
  private static final String FETCH_AND_LOCK_RESOURCE_PATH = "/external-task/fetchAndLock";

  private String endpointUrl;
  private String workerId;
  private EngineInteractionManager engineInteraction;

  public EngineClient(String workerId, String endpointUrl) {
    this.workerId = workerId;
    this.engineInteraction = new EngineInteractionManager();
    this.endpointUrl = engineInteraction.sanitizeUrl(endpointUrl);
  }

  public List<LockedTask> fetchAndLock(List<TaskTopicRequestDto> topics) {
    FetchAndLockRequestDto payload = new FetchAndLockRequestDto(workerId, MAX_TASKS, topics);
    String resourceUrl = endpointUrl + FETCH_AND_LOCK_RESOURCE_PATH;
    LockedTask[] lockedTasksResponse = engineInteraction.postRequest(resourceUrl, payload, LockedTaskDto[].class);
    return Arrays.asList(lockedTasksResponse);
  }

  public String getEndpointUrl() {
    return endpointUrl;
  }

  public String getWorkerId() {
    return workerId;
  }

}
