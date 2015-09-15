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
package org.camunda.bpm.engine.test.api.task;

import java.util.Date;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.task.TaskQuery;

/**
 * @author Thorben Lindhauer
 *
 */
public class TaskQueryDisabledStoredExpressionsTest extends ResourceProcessEngineTestCase {


  protected static final String EXPECTED_STORED_QUERY_FAILURE_MESSAGE =
      "Expressions are forbidden in stored queries. This behavior can be toggled in the process engine configuration";
  public static final String STATE_MANIPULATING_EXPRESSION =
      "${''.getClass().forName('" + TaskQueryDisabledStoredExpressionsTest.class.getName() + "').getField('MUTABLE_FIELD').setLong(null, 42)}";

  public static long MUTABLE_FIELD = 0;

  public TaskQueryDisabledStoredExpressionsTest() {
    super("org/camunda/bpm/engine/test/api/task/task-query-disabled-stored-expressions-test.camunda.cfg.xml");
  }

  protected void setUp() throws Exception {
    MUTABLE_FIELD = 0;
  }

  public void testStoreFilterWithoutExpression() {
    TaskQuery taskQuery = taskService.createTaskQuery().dueAfter(new Date());
    Filter filter = filterService.newTaskFilter("filter");
    filter.setQuery(taskQuery);

    // saving the filter suceeds
    filterService.saveFilter(filter);
    assertEquals(1, filterService.createFilterQuery().count());

    // cleanup
    filterService.deleteFilter(filter.getId());
  }

  public void testStoreFilterWithExpression() {
    TaskQuery taskQuery = taskService.createTaskQuery().dueAfterExpression(STATE_MANIPULATING_EXPRESSION);
    Filter filter = filterService.newTaskFilter("filter");
    filter.setQuery(taskQuery);

    try {
      filterService.saveFilter(filter);
    } catch (ProcessEngineException e) {
      assertTextPresent(EXPECTED_STORED_QUERY_FAILURE_MESSAGE, e.getMessage());
    }
    assertTrue(fieldIsUnchanged());
  }

  public void testUpdateFilterWithExpression() {
    // given a stored filter
    TaskQuery taskQuery = taskService.createTaskQuery().dueAfter(new Date());
    Filter filter = filterService.newTaskFilter("filter");
    filter.setQuery(taskQuery);
    filterService.saveFilter(filter);

    // updating the filter with an expression does not suceed
    filter.setQuery(taskQuery.dueBeforeExpression(STATE_MANIPULATING_EXPRESSION));
    assertEquals(1, filterService.createFilterQuery().count());

    try {
      filterService.saveFilter(filter);
    } catch (ProcessEngineException e) {
      assertTextPresent(EXPECTED_STORED_QUERY_FAILURE_MESSAGE, e.getMessage());
    }
    assertTrue(fieldIsUnchanged());

    // cleanup
    filterService.deleteFilter(filter.getId());
  }

  public void testCannotExecuteStoredFilter() {
    final TaskQuery filterQuery = taskService.createTaskQuery().dueAfterExpression(STATE_MANIPULATING_EXPRESSION);

    // store a filter bypassing validation
    // the API way of doing this would be by reconfiguring the engine
    String filterId = processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<String>() {

      public String execute(CommandContext commandContext) {
        FilterEntity filter = new FilterEntity(EntityTypes.TASK);
        filter.setQuery(filterQuery);
        filter.setName("filter");
        commandContext.getDbEntityManager().insert(filter);
        return filter.getId();
      }
    });

    extendFilterAndValidateFailingQuery(filterId, null);

    // cleanup
    filterService.deleteFilter(filterId);
  }

  protected boolean fieldIsUnchanged() {
    return MUTABLE_FIELD == 0;
  }

  protected void extendFilterAndValidateFailingQuery(String filterId, TaskQuery query) {
    try {
      filterService.list(filterId, query);
    } catch (BadUserRequestException e) {
      assertTextPresent(EXPECTED_STORED_QUERY_FAILURE_MESSAGE, e.getMessage());
    }

    assertTrue(fieldIsUnchanged());

    try {
      filterService.count(filterId, query);
    } catch (BadUserRequestException e) {
      assertTextPresent(EXPECTED_STORED_QUERY_FAILURE_MESSAGE, e.getMessage());
    }

    assertTrue(fieldIsUnchanged());
  }
}
