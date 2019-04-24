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
package org.camunda.bpm.engine.externaltask;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public interface UpdateExternalTaskRetriesSelectBuilder {

  /**
   * Selects a list of external tasks with the given list of ids.
   */
  UpdateExternalTaskRetriesBuilder externalTaskIds(List<String> externalTaskIds);

  /**
   * Selects a list of external tasks with the given list of ids.
   */
  UpdateExternalTaskRetriesBuilder externalTaskIds(String... externalTaskIds);

  /**
   * Selects a list of external tasks with the given list of process instances ids.
   */
  UpdateExternalTaskRetriesBuilder processInstanceIds(List<String> processInstanceIds);

  /**
   * Selects a list of external tasks with the given list of process instances ids.
   */
  UpdateExternalTaskRetriesBuilder processInstanceIds(String... processInstanceIds);

  /**
   * Selects a list of external tasks with the given external task query.
   */
  UpdateExternalTaskRetriesBuilder externalTaskQuery(ExternalTaskQuery externalTaskQuery);

  /**
   * Selects a list of external tasks with the given process instance query.
   */
  UpdateExternalTaskRetriesBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery);

  /**
   * Selects a list of external tasks with the given historic process instance query.
   */
  UpdateExternalTaskRetriesBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery historicProcessInstanceQuery);

}
