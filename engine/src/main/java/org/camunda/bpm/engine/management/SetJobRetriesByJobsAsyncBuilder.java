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
import org.camunda.bpm.engine.runtime.JobQuery;

/**
 * Fluent builder to update the number of retries for one or multiple jobs asynchronously.
 */
public interface SetJobRetriesByJobsAsyncBuilder extends SetJobRetriesAsyncBuilder {

  /**
   * Specifies a {@link JobQuery} to reference jobs that will be updated.
   *
   * <strong>Note:</strong> You can reference jobs either by specific jobs ({@link #jobQuery}, {@link #jobIds}) or by process ({@link #processInstanceIds}, {@link #processInstanceQuery}, {@link #historicProcessInstanceQuery}).
   * If you provide a job query, calling {@link #processInstanceIds}, {@link #processInstanceQuery} or {@link #historicProcessInstanceQuery} are not allowed and will cause a {@link ProcessEngineException} on {@link #executeAsync()}.
   *
   * @param query the query to reference jobs that will be updated.
   *
   * @see ManagementService#setJobRetriesAsync(JobQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesByJobsAsyncBuilder jobQuery(JobQuery query);

  /**
   * Specifies a list of job ids that will be updated.
   *
   * <strong>Note:</strong> You can reference jobs either by specific jobs ({@link #jobQuery}, {@link #jobIds}) or by process ({@link #processInstanceIds}, {@link #processInstanceQuery}, {@link #historicProcessInstanceQuery}).
   * If you provide a list of jobs, calling {@link #processInstanceIds}, {@link #processInstanceQuery} or {@link #historicProcessInstanceQuery} are not allowed and will cause a {@link ProcessEngineException} on {@link #executeAsync()}.
   *
   * @param jobIds The list of job ids that will be updated.
   *
   * @see ManagementService#setJobRetriesAsync(JobQuery, int)
   *
   * @return the builder instance
   */
  SetJobRetriesByJobsAsyncBuilder jobIds(List<String> jobIds);
}
