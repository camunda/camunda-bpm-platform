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
package org.camunda.bpm.client.topic.impl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.camunda.bpm.client.impl.RequestDto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tassilo Weidner
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchAndLockRequestDto extends RequestDto {

  protected int maxTasks;
  protected boolean usePriority;
  protected Long asyncResponseTimeout;
  protected List<TopicRequestDto> topics = new ArrayList<>();

  public FetchAndLockRequestDto(String workerId, int maxTasks, Long asyncResponseTimeout, List<TopicRequestDto> topics) {
    this(workerId, maxTasks, asyncResponseTimeout, topics, true);
  }

  public FetchAndLockRequestDto(String workerId, int maxTasks, Long asyncResponseTimeout, List<TopicRequestDto> topics, boolean usePriority) {
    super(workerId);
    this.maxTasks = maxTasks;
    this.usePriority = usePriority;
    this.asyncResponseTimeout = asyncResponseTimeout;
    this.topics = topics;
  }

  public int getMaxTasks() {
    return maxTasks;
  }

  public boolean isUsePriority() {
    return usePriority;
  }

  public List<TopicRequestDto> getTopics() {
    return topics;
  }

  public Long getAsyncResponseTimeout() {
    return asyncResponseTimeout;
  }

}
