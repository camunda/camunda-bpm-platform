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

import java.util.List;

import org.camunda.bpm.engine.impl.IncidentQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * @author roman.smirnov
 */
public class IncidentManager extends AbstractManager {

  @SuppressWarnings("unchecked")
  public List<Incident> findIncidentsByExecution(String id) {
    return getDbSqlSession().selectList("selectIncidentsByExecutionId", id);
  }
  
  public long findIncidentCountByQueryCriteria(IncidentQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectIncidentCountByQueryCriteria", jobQuery);
  }
  
  @SuppressWarnings("unchecked")
  public List<Incident> findIncidentByConfiguration(String configuration) {
    return getDbSqlSession().selectList("selectIncidentsByConfiguration", configuration);
  }
  
  @SuppressWarnings("unchecked")
  public List<Incident> findIncidentByQueryCriteria(IncidentQueryImpl jobQuery, Page page) {
    return getDbSqlSession().selectList("selectIncidentByQueryCriteria", jobQuery, page);
  }

}
