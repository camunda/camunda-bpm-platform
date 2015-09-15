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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.FILTER;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.QueryValidators.StoredQueryValidator;
import org.camunda.bpm.engine.impl.filter.FilterQueryImpl;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;

/**
 * @author Sebastian Menski
 */
public class FilterManager extends AbstractManager {

  public Filter createNewFilter(String resourceType) {
    checkAuthorization(CREATE, FILTER, ANY);
    return new FilterEntity(resourceType);
  }

  public Filter insertOrUpdateFilter(Filter filter) {

    AbstractQuery<?, ?> query = filter.getQuery();
    query.validate(StoredQueryValidator.get());

    if (filter.getId() == null) {
      checkAuthorization(CREATE, FILTER, ANY);
      getDbEntityManager().insert((FilterEntity) filter);
      createDefaultAuthorizations(filter);
    }
    else {
      checkAuthorization(UPDATE, FILTER, filter.getId());
      getDbEntityManager().merge((FilterEntity) filter);
    }

    return filter;
  }

  public void deleteFilter(String filterId) {
    checkAuthorization(DELETE, FILTER, filterId);

    FilterEntity filter = findFilterByIdInternal(filterId);
    ensureNotNull("No filter found for filter id '" + filterId + "'", "filter", filter);

    // delete all authorizations for this filter id
    deleteAuthorizations(FILTER, filterId);
    // delete the filter itself
    getDbEntityManager().delete(filter);
  }

  public FilterEntity findFilterById(String filterId) {
    ensureNotNull("Invalid filter id", "filterId", filterId);
    checkAuthorization(READ, FILTER, filterId);
    return findFilterByIdInternal(filterId);
  }

  protected FilterEntity findFilterByIdInternal(String filterId) {
    return getDbEntityManager().selectById(FilterEntity.class, filterId);
  }

  @SuppressWarnings("unchecked")
  public List<Filter> findFiltersByQueryCriteria(FilterQueryImpl filterQuery) {
    configureQuery(filterQuery, FILTER);
    return getDbEntityManager().selectList("selectFilterByQueryCriteria", filterQuery);
  }

  public long findFilterCountByQueryCriteria(FilterQueryImpl filterQuery) {
    configureQuery(filterQuery, FILTER);
    return (Long) getDbEntityManager().selectOne("selectFilterCountByQueryCriteria", filterQuery);
  }

  // authorization utils /////////////////////////////////

  protected void createDefaultAuthorizations(Filter filter) {
    if(isAuthorizationEnabled()) {
      saveDefaultAuthorizations(getResourceAuthorizationProvider().newFilter(filter));
    }
  }
}
