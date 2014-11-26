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
package org.camunda.bpm.engine.impl.cmmn.entity.runtime;

import java.util.List;

import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

/**
 * @author Roman Smirnov
 *
 */
public class CaseSentryPartManager extends AbstractManager {

  public void insertCaseSentryPart(CaseSentryPartEntity caseSentryPart) {
    getDbEntityManager().insert(caseSentryPart);
  }

  public void deleteSentryPart(CaseSentryPartEntity caseSentryPart) {
    getDbEntityManager().delete(caseSentryPart);
  }

  @SuppressWarnings("unchecked")
  public List<CaseSentryPartEntity> findCaseSentryPartsByCaseExecutionId(String caseExecutionId) {
    return getDbEntityManager().selectList("selectCaseSentryPartsByCaseExecutionId", caseExecutionId);
  }

  public long findCaseSentryPartCountByQueryCriteria(CaseSentryPartQueryImpl caseSentryPartQuery) {
    return (Long) getDbEntityManager().selectOne("selectCaseSentryPartsCountByQueryCriteria", caseSentryPartQuery);
  }

  @SuppressWarnings("unchecked")
  public List<CaseSentryPartEntity> findCaseSentryPartByQueryCriteria(CaseSentryPartQueryImpl caseSentryPartQuery, Page page) {
    return getDbEntityManager().selectList("selectCaseSentryPartsByQueryCriteria", caseSentryPartQuery, page);
  }

}
