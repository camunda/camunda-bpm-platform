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
    return getDbEntityManager().selectListWithRawParameter(
      "selectTaskCountByCandidateGroupReportQuery",
      query,
      0,
      Integer.MAX_VALUE
    );
  }

  protected void configureQuery(TaskReportImpl parameter) {
    getAuthorizationManager().checkAuthorization(Permissions.READ, Resources.TASK, Authorization.ANY);
    getTenantManager().configureTenantCheck(parameter.getTenantCheck());
  }

}
