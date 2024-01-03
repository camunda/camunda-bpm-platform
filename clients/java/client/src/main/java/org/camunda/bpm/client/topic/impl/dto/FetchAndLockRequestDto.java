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
import java.util.List;
import org.camunda.bpm.client.impl.RequestDto;
import org.camunda.bpm.client.task.OrderingConfig;
import org.camunda.bpm.client.task.SortingDto;

/**
 * @author Tassilo Weidner
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FetchAndLockRequestDto extends RequestDto {

  protected int maxTasks;
  protected boolean usePriority;
  protected Long asyncResponseTimeout;
  protected List<TopicRequestDto> topics;
  protected List<SortingDto> sorting;

  public FetchAndLockRequestDto(String workerId, int maxTasks, Long asyncResponseTimeout, List<TopicRequestDto> topics) {
    this(workerId, maxTasks, asyncResponseTimeout, topics, true);
  }

  public FetchAndLockRequestDto(String workerId, int maxTasks, Long asyncResponseTimeout, List<TopicRequestDto> topics,
                                boolean usePriority) {
    this(workerId, maxTasks, asyncResponseTimeout, topics, usePriority, OrderingConfig.empty());
  }

  public FetchAndLockRequestDto(String workerId, int maxTasks, Long asyncResponseTimeout, List<TopicRequestDto> topics,
                                boolean usePriority, OrderingConfig orderingConfig) {
    super(workerId);
    this.maxTasks = maxTasks;
    this.usePriority = usePriority;
    this.asyncResponseTimeout = asyncResponseTimeout;
    this.topics = topics;
    this.sorting = orderingConfig.toSortingDtos();
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

  public List<SortingDto> getSorting() {
    return sorting;
  }

  public void setSorting(List<SortingDto> sorting) {
    this.sorting = sorting;
  }

}
