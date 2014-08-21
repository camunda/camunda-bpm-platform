/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.filter.FilterQueryImpl;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

/**
 * @author Sebastian Menski
 */
public class FilterManager extends AbstractManager {

  public FilterEntity findFilterById(String filterId) {
    ensureNotNull("Invalid filter id", "filterId", filterId);
    return getDbEntityManager().selectById(FilterEntity.class, filterId);
  }

  @SuppressWarnings("unchecked")
  public List<Filter> findFiltersByQueryCriteria(FilterQueryImpl filterQuery) {
    return getDbEntityManager().selectList("selectFilterByQueryCriteria", filterQuery);
  }

  public long findFilterCountByQueryCriteria(FilterQueryImpl filterQuery) {
    return (Long) getDbEntityManager().selectOne("selectFilterCountByQueryCriteria", filterQuery);
  }

}
