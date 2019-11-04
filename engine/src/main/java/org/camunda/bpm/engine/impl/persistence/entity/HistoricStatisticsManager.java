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
package org.camunda.bpm.engine.impl.persistence.entity;

import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricCaseActivityStatistics;
import org.camunda.bpm.engine.impl.HistoricActivityStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.HistoricCaseActivityStatisticsQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

/**
 *
 * @author Roman Smirnov
 *
 */
public class HistoricStatisticsManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<HistoricActivityStatistics> getHistoricStatisticsGroupedByActivity(HistoricActivityStatisticsQueryImpl query, Page page) {
    if (ensureHistoryReadOnProcessDefinition(query)) {
      if (query.isIncludeClosedIncidents()) {
        return getStatisticsWithIncidents(query, page);
      }
      return getDbEntityManager().selectList("selectHistoricActivityStatistics", query, page);
    }
    else {
      return new ArrayList<HistoricActivityStatistics>();
    }
  }

  public long getHistoricStatisticsCountGroupedByActivity(HistoricActivityStatisticsQueryImpl query) {
    if (ensureHistoryReadOnProcessDefinition(query)) {
      return (Long) getDbEntityManager().selectOne("selectHistoricActivityStatisticsCount", query);
    }
    else {
      return 0;
    }
  }

  @SuppressWarnings("unchecked")
  public List<HistoricCaseActivityStatistics> getHistoricStatisticsGroupedByCaseActivity(HistoricCaseActivityStatisticsQueryImpl query, Page page) {
    return getDbEntityManager().selectList("selectHistoricCaseActivityStatistics", query, page);
  }

  public long getHistoricStatisticsCountGroupedByCaseActivity(HistoricCaseActivityStatisticsQueryImpl query) {
    return (Long) getDbEntityManager().selectOne("selectHistoricCaseActivityStatisticsCount", query);
  }

  protected boolean ensureHistoryReadOnProcessDefinition(HistoricActivityStatisticsQueryImpl query) {
    CommandContext commandContext = getCommandContext();

    if(isAuthorizationEnabled() && getCurrentAuthentication() != null && commandContext.isAuthorizationCheckEnabled()) {
      String processDefinitionId = query.getProcessDefinitionId();
      ProcessDefinitionEntity definition = getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);

      if (definition == null) {
        return false;
      }

      return getAuthorizationManager().isAuthorized(READ_HISTORY, PROCESS_DEFINITION, definition.getKey());
    }

    return true;
  }

  // CAM-10892
  // the historic incidents cannot be added to the statistic query due to lack of activity instance id in the incident
  // so a second query is perform to fetch the incidents group by activity id and the result is merged
  @SuppressWarnings("unchecked")
  protected List<HistoricActivityStatistics> getStatisticsWithIncidents(HistoricActivityStatisticsQueryImpl query, Page page) {
    // this is sorted list
    List<HistoricActivityStatisticsImpl> statsWithoutIncidents = getDbEntityManager().selectList("selectHistoricActivityStatistics", query, page);
    // fetch incidents per activity id by applying the same filters as the statistic query
    List<HistoricActivityStatistics> incidentStats = getDbEntityManager().selectList("selectHistoricActivityStatisticsWithIncident", query, page);
    Map<String, Long> incidentsMap = convertIncidentStatisticsToMap(incidentStats);

    List<HistoricActivityStatistics> mergedResult = new ArrayList<>();

    for (HistoricActivityStatisticsImpl stats : statsWithoutIncidents) {
      if(incidentsMap.get(stats.getId()) != null) {
        stats.setClosedIncidents(incidentsMap.get(stats.getId()).longValue());
      }
      mergedResult.add(stats); // the sorting is preserved
    }
    return mergedResult;
  }

  protected Map<String, Long> convertIncidentStatisticsToMap(List<HistoricActivityStatistics> incidentStats) {
    Map<String, Long> incidentsMap = new HashMap<>();
    for (HistoricActivityStatistics incidentStat : incidentStats) {
      incidentsMap.put(incidentStat.getId(), incidentStat.getClosedIncidents());
    }
    return incidentsMap;
  }

}
