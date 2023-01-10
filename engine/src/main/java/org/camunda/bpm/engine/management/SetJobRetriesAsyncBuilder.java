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

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

/**
 * Fluent builder to update the number of retries for one or multiple jobs asynchronously.
 */
public interface SetJobRetriesAsyncBuilder {

  /**
   * Specifies a {@link JobQuery} to reference jobs that will be updated.
   *
   * <strong>Note:</strong> You can reference jobs either by specific jobs ({@link #jobQuery}, {@link #jobIds}) or by process ({@link #processInstanceIds}, {@link #processInstanceQuery}, {@link #historicProcessInstanceQuery}).
   * If you provide a job query, calling {@link #processInstanceIds}, {@link #processInstanceQuery} or {@link #historicProcessInstanceQuery} are not allowed and will cause a {@link ProcessEngineException} on {@link #execute()}.
   *
   * @param query the query to reference jobs that will be updated.
   *
   * @see ManagementService#setJobRetriesAsync(JobQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesAsyncBuilder jobQuery(JobQuery query);

  /**
   * Specifies a list of process instance ids. All jobs belonging to those process instances will be updated.
   *
   * <strong>Note:</strong> You can reference jobs either by specific jobs ({@link #jobQuery}, {@link #jobIds}) or by process ({@link #processInstanceIds}, {@link #processInstanceQuery}, {@link #historicProcessInstanceQuery}).
   * If you provide processInstanceIds, calling {@link #jobQuery} or {@link #jobIds} is not allowed and will cause a {@link ProcessEngineException} on {@link #execute()}.
   *
   * @param processInstanceIds the list of process instance ids
   *
   * @see ManagementService#setJobRetriesAsync(List, ProcessInstanceQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesAsyncBuilder processInstanceIds(List<String> processInstanceIds);

  /**
   * Specifies a process instance query. All jobs belonging to those process instances will be updated.
   *
   * <strong>Note:</strong> You can reference jobs either by specific jobs ({@link #jobQuery}, {@link #jobIds}) or by process ({@link #processInstanceIds}, {@link #processInstanceQuery}, {@link #historicProcessInstanceQuery}).
   * If you provide a processInstanceQuery, calling {@link #jobQuery} or {@link #jobIds} is not allowed and will cause a {@link ProcessEngineException} on {@link #execute()}.
   *
   * @param query the process instance query
   *
   * @see ManagementService#setJobRetriesAsync(List, ProcessInstanceQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesAsyncBuilder processInstanceQuery(ProcessInstanceQuery query);

  /**
   * Specifies a historic process instance query that identifies runtime process instances with jobs that have to be modified. All jobs belonging to those process instances will be updated.
   *
   * <strong>Note:</strong> You can reference jobs either by specific jobs ({@link #jobQuery}, {@link #jobIds}) or by process ({@link #processInstanceIds}, {@link #processInstanceQuery}, {@link #historicProcessInstanceQuery}).
   * If you provide a historicProcessInstanceQuery, calling {@link #jobQuery} or {@link #jobIds} is not allowed and will cause a {@link ProcessEngineException} on {@link #execute()}.
   *
   * @param query the historic process instance
   *
   * @see ManagementService#setJobRetriesAsync(List, ProcessInstanceQuery, HistoricProcessInstanceQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesAsyncBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery query);

  /**
   * Specifies a list of job ids that will be updated.
   *
   * <strong>Note:</strong> You can reference jobs either by specific jobs ({@link #jobQuery}, {@link #jobIds}) or by process ({@link #processInstanceIds}, {@link #processInstanceQuery}, {@link #historicProcessInstanceQuery}).
   * If you provide a list of jobs, calling {@link #processInstanceIds}, {@link #processInstanceQuery} or {@link #historicProcessInstanceQuery} are not allowed and will cause a {@link ProcessEngineException} on {@link #execute()}.
   *
   * @param query the query to reference jobs that will be updated.
   *
   * @see ManagementService#setJobRetriesAsync(JobQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesAsyncBuilder jobIds(List<String> jobIds);

  /**
   * Specifies a due date to be set on the referenced {@link Job jobs}.
   *
   * When the number of retries of a job are incremented it is not automatically scheduled for immediate execution.
   * When a {@link Job} is executed is determined by the due date. By setting the due date together with the job retries, the scheduled execution date of the
   * job can be adjusted.
   *
   * @param the new due date for the updated jobs. If it is null, the retries of the jobs will be updated but the due date is ignored.
   *
   * @return the builder instance
   */
  SetJobRetriesAsyncBuilder dueDate(Date dueDate);

  /**
   * Closes the fluent builder and executes the instructions.
   *
   * @throws ProcessEngineException Check the builder methods and their linked counterparts in {@link ManagementService} for more detail
   */
  Batch execute();
}
