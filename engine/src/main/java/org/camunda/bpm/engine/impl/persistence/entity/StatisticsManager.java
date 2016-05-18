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

import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.batch.BatchStatistics;
import org.camunda.bpm.engine.impl.ActivityStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.DeploymentStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.ProcessDefinitionStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;

public class StatisticsManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<ProcessDefinitionStatistics> getStatisticsGroupedByProcessDefinitionVersion(ProcessDefinitionStatisticsQueryImpl query, Page page) {
    configureQuery(query);
    return getDbEntityManager().selectList("selectProcessDefinitionStatistics", query, page);
  }

  public long getStatisticsCountGroupedByProcessDefinitionVersion(ProcessDefinitionStatisticsQueryImpl query) {
    configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectProcessDefinitionStatisticsCount", query);
  }

  @SuppressWarnings("unchecked")
  public List<ActivityStatistics> getStatisticsGroupedByActivity(ActivityStatisticsQueryImpl query, Page page) {
    configureQuery(query);
    return getDbEntityManager().selectList("selectActivityStatistics", query, page);
  }

  public long getStatisticsCountGroupedByActivity(ActivityStatisticsQueryImpl query) {
    configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectActivityStatisticsCount", query);
  }

  @SuppressWarnings("unchecked")
  public List<DeploymentStatistics> getStatisticsGroupedByDeployment(DeploymentStatisticsQueryImpl query, Page page) {
    configureQuery(query);
    return getDbEntityManager().selectList("selectDeploymentStatistics", query, page);
  }

  public long getStatisticsCountGroupedByDeployment(DeploymentStatisticsQueryImpl query) {
    configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectDeploymentStatisticsCount", query);
  }

  @SuppressWarnings("unchecked")
  public List<BatchStatistics> getStatisticsGroupedByBatch(BatchStatisticsQueryImpl query, Page page) {
    configureQuery(query);
    return getDbEntityManager().selectList("selectBatchStatistics", query, page);
  }

  public long getStatisticsCountGroupedByBatch(BatchStatisticsQueryImpl query) {
    configureQuery(query);
    return (Long) getDbEntityManager().selectOne("selectBatchStatisticsCount", query);
  }

  protected void configureQuery(DeploymentStatisticsQueryImpl query) {
    getAuthorizationManager().configureDeploymentStatisticsQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected void configureQuery(ProcessDefinitionStatisticsQueryImpl query) {
    getAuthorizationManager().configureProcessDefinitionStatisticsQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected void configureQuery(ActivityStatisticsQueryImpl query) {
    checkReadProcessDefinition(query);
    getAuthorizationManager().configureActivityStatisticsQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected void configureQuery(BatchStatisticsQueryImpl batchQuery) {
    getAuthorizationManager().configureBatchStatisticsQuery(batchQuery);
    getTenantManager().configureQuery(batchQuery);
  }

  protected void checkReadProcessDefinition(ActivityStatisticsQueryImpl query) {
    CommandContext commandContext = getCommandContext();
    if(isAuthorizationEnabled() && getCurrentAuthentication() != null && commandContext.isAuthorizationCheckEnabled()) {
      String processDefinitionId = query.getProcessDefinitionId();
      ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
      ensureNotNull("no deployed process definition found with id '" + processDefinitionId + "'", "processDefinition", definition);
      getAuthorizationManager().checkAuthorization(READ, PROCESS_DEFINITION, definition.getKey());
    }
  }
}
