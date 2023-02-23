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
package org.camunda.bpm.engine.management;

import java.util.List;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

/**
 * Fluent builder to update the number of retries for one or multiple jobs referenced by process asynchronously.
 */
public interface SetJobRetriesByProcessAsyncBuilder extends SetJobRetriesAsyncBuilder {

  /**
   * Specifies a list of process instance ids. All jobs belonging to those process instances will be updated. If this
   * method is called together with more methods referencing jobs, the builder will operate on the jobs referenced by
   * all. If no method referencing jobs is called, a {@link ProcessEngineException} is thrown on
   * {@link #executeAsync()}.
   *
   * @param processInstanceIds
   *          the list of process instance ids
   *
   * @see ManagementService#setJobRetriesAsync(List, ProcessInstanceQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesByProcessAsyncBuilder processInstanceIds(List<String> processInstanceIds);

  /**
   * Specifies a process instance query. All jobs belonging to those process instances will be updated. If this method
   * is called together with more methods referencing jobs, the builder will operate on the jobs referenced by all. If
   * no method referencing jobs is called, a {@link ProcessEngineException} is thrown on {@link #executeAsync()}.
   *
   * @param query
   *          the process instance query
   *
   * @see ManagementService#setJobRetriesAsync(List, ProcessInstanceQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesByProcessAsyncBuilder processInstanceQuery(ProcessInstanceQuery query);

  /**
   * Specifies a historic process instance query that identifies runtime process instances with jobs that have to be
   * modified. All jobs belonging to those process instances will be updated. If this method is called together with
   * more methods referencing jobs, the builder will operate on the jobs referenced by all. If no method referencing
   * jobs is called, a {@link ProcessEngineException} is thrown on {@link #executeAsync()}.
   *
   * @param query
   *          the historic process instance
   *
   * @see ManagementService#setJobRetriesAsync(List, ProcessInstanceQuery, HistoricProcessInstanceQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesByProcessAsyncBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery query);
}
