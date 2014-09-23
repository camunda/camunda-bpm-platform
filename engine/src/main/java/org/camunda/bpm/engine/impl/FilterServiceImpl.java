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
package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.filter.FilterQuery;
import org.camunda.bpm.engine.impl.cmd.CreateFilterCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteFilterCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteFilterCountCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteFilterListCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteFilterListPageCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteFilterSingleResultCmd;
import org.camunda.bpm.engine.impl.cmd.GetFilterCmd;
import org.camunda.bpm.engine.impl.cmd.SaveFilterCmd;
import org.camunda.bpm.engine.impl.filter.FilterQueryImpl;
import org.camunda.bpm.engine.query.Query;


/**
 * @author Sebastian Menski
 */
public class FilterServiceImpl extends ServiceImpl implements FilterService {

  public Filter newTaskFilter() {
    return commandExecutor.execute(new CreateFilterCmd(EntityTypes.TASK));
  }

  public Filter newTaskFilter(String filterName) {
    return newTaskFilter().setName(filterName);
  }

  public FilterQuery createFilterQuery() {
    return new FilterQueryImpl(commandExecutor);
  }

  public FilterQuery createTaskFilterQuery() {
    return new FilterQueryImpl(commandExecutor).filterResourceType(EntityTypes.TASK);
  }

  public Filter saveFilter(Filter filter) {
    return commandExecutor.execute(new SaveFilterCmd(filter));
  }

  public Filter getFilter(String filterId) {
    return commandExecutor.execute(new GetFilterCmd(filterId));
  }

  public void deleteFilter(String filterId) {
    commandExecutor.execute(new DeleteFilterCmd(filterId));
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> list(String filterId) {
    return (List<T>) commandExecutor.execute(new ExecuteFilterListCmd(filterId));
  }

  @SuppressWarnings("unchecked")
  public <T, Q extends Query<?, T>> List<T> list(String filterId, Q extendingQuery) {
    return (List<T>) commandExecutor.execute(new ExecuteFilterListCmd(filterId, extendingQuery));
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> listPage(String filterId, int firstResult, int maxResults) {
    return (List<T>) commandExecutor.execute(new ExecuteFilterListPageCmd(filterId, firstResult, maxResults));
  }

  @SuppressWarnings("unchecked")
  public <T, Q extends Query<?, T>> List<T> listPage(String filterId, Q extendingQuery, int firstResult, int maxResults) {
    return (List<T>) commandExecutor.execute(new ExecuteFilterListPageCmd(filterId, extendingQuery, firstResult, maxResults));
  }

  @SuppressWarnings("unchecked")
  public <T> T singleResult(String filterId) {
    return (T) commandExecutor.execute(new ExecuteFilterSingleResultCmd(filterId));
  }

  @SuppressWarnings("unchecked")
  public <T, Q extends Query<?, T>> T singleResult(String filterId, Q extendingQuery) {
    return (T) commandExecutor.execute(new ExecuteFilterSingleResultCmd(filterId, extendingQuery));
  }

  public Long count(String filterId) {
    return commandExecutor.execute(new ExecuteFilterCountCmd(filterId));
  }

  public Long count(String filterId, Query<?, ?> extendingQuery) {
    return commandExecutor.execute(new ExecuteFilterCountCmd(filterId, extendingQuery));
  }

}
