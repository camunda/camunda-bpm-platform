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
package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.DurationReportResult;
import org.camunda.bpm.engine.history.HistoricTaskInstanceReportResult;
import org.camunda.bpm.engine.impl.HistoricTaskInstanceReportImpl;
import org.camunda.bpm.engine.impl.TaskReportImpl;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.task.TaskCountByCandidateGroupResult;

import java.util.List;

/**
 * @author Stefan Hentschel
 *
 */
public class TaskReportManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<TaskCountByCandidateGroupResult> createTaskCountByCandidateGroupReport(TaskReportImpl query) {
    configureQuery(query);
    return getDbEntityManager().selectListWithRawParameter("selectTaskCountByCandidateGroupReportQuery", query, 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstanceReportResult> selectHistoricTaskInstanceCountByTaskNameReport(HistoricTaskInstanceReportImpl query) {
    configureQuery(query);
    return getDbEntityManager().selectListWithRawParameter("selectHistoricTaskInstanceCountByTaskNameReport", query, 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstanceReportResult> selectHistoricTaskInstanceCountByProcDefKeyReport(HistoricTaskInstanceReportImpl query) {
    configureQuery(query);
    return getDbEntityManager().selectListWithRawParameter("selectHistoricTaskInstanceCountByProcDefKeyReport", query, 0, Integer.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public List<DurationReportResult> createHistoricTaskDurationReport(HistoricTaskInstanceReportImpl query) {
    configureQuery(query);
    return getDbEntityManager().selectListWithRawParameter("selectHistoricTaskInstanceDurationReport", query, 0, Integer.MAX_VALUE);
  }

  protected void configureQuery(HistoricTaskInstanceReportImpl parameter) {
    getAuthorizationManager().checkAuthorization(Permissions.READ_HISTORY, Resources.TASK, Authorization.ANY);
    getTenantManager().configureTenantCheck(parameter.getTenantCheck());
  }

  protected void configureQuery(TaskReportImpl parameter) {
    getAuthorizationManager().checkAuthorization(Permissions.READ, Resources.TASK, Authorization.ANY);
    getTenantManager().configureTenantCheck(parameter.getTenantCheck());
  }

}
