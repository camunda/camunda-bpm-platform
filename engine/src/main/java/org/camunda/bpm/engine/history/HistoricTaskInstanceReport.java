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
package org.camunda.bpm.engine.history;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.query.Report;

/**
 * @author Stefan Hentschel.
 */
public interface HistoricTaskInstanceReport extends Report {

  /**
   * <p>Sets the completed after date for constraining the query to search for all tasks
   * which are completed after a certain date.</p>
   *
   * @param completedAfter A {@link Date} to define the granularity of the report
   *
   * @throws NotValidException
   *          When the given date is null.
   */
  HistoricTaskInstanceReport completedAfter(Date completedAfter);

  /**
   * <p>Sets the completed before date for constraining the query to search for all tasks
   * which are completed before a certain date.</p>
   *
   * @param completedBefore A {@link Date} to define the granularity of the report
   *
   * @throws NotValidException
   *          When the given date is null.
   */
  HistoricTaskInstanceReport completedBefore(Date completedBefore);

  /**
   * <p>Executes the task report query and returns a list of {@link HistoricTaskInstanceReportResult}s</p>
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ_HISTORY} permission
   *          on any {@link Resources#PROCESS_DEFINITION}.
   *
   * @return a list of {@link HistoricTaskInstanceReportResult}s
   */
  List<HistoricTaskInstanceReportResult> countByProcessDefinitionKey();

  /**
   * <p>Executes the task report query and returns a list of {@link HistoricTaskInstanceReportResult}s</p>
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ_HISTORY} permission
   *          on any {@link Resources#PROCESS_DEFINITION}.
   *
   * @return a list of {@link HistoricTaskInstanceReportResult}s
   */
  List<HistoricTaskInstanceReportResult> countByTaskName();
}
