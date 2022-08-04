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

import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.task.ExternalTask;

import java.util.Collections;
import java.util.List;

public class FetchAndLockResponseDto {

  protected List<ExternalTask> externalTasks;
  protected ExternalTaskClientException error;

  public FetchAndLockResponseDto(List<ExternalTask> externalTasks) {
    this.externalTasks = externalTasks;
  }

  public FetchAndLockResponseDto(ExternalTaskClientException error) {
    this.externalTasks = Collections.emptyList();
    this.error = error;
  }

  public List<ExternalTask> getExternalTasks() {
    return externalTasks;
  }

  public boolean hasError() {
    return error != null;
  }

  public ExternalTaskClientException getError() {
    return error;
  }
}
