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

package org.camunda.bpm.engine.test.api.filter;

import java.util.HashMap;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.task.TaskQuery;

/**
 * @author Sebastian Menski
 */
public class FilterServiceTest extends PluggableProcessEngineTestCase {

  protected Filter filter;

  public void setUp() {
    filter = filterService.newTaskFilter()
      .setName("name")
      .setOwner("owner")
      .setQuery(taskService.createTaskQuery())
      .setProperties(new HashMap<String, Object>());
    assertNull(filter.getId());
    filterService.saveFilter(filter);
    assertNotNull(filter.getId());
  }

  public void tearDown() {
    // delete all existing filters
    for (Filter filter : filterService.createTaskFilterQuery().list()) {
      filterService.deleteFilter(filter.getId());
    }
  }

  public void testCreateFilter() {
    assertNotNull(filter);

    Filter filter2 = filterService.getFilter(filter.getId());
    assertNotNull(filter2);

    compareFilter(filter, filter2);
  }

  public void testCreateInvalidFilter() {
    try {
      filter.setName(null);
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }

    try {
      filter.setName("");
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }

    try {
      filter.setQuery((Query<?, ?>) null);
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }
  }

  public void testUpdateFilter() {
    filter.setName("newName");
    filter.setOwner("newOwner");
    filter.setQuery(taskService.createTaskQuery());
    filter.setProperties(new HashMap<String, Object>());

    filterService.saveFilter(filter);

    Filter filter2 = filterService.getFilter(filter.getId());

    compareFilter(filter, filter2);
  }

  public void testExtendFilter() {
    TaskQuery extendingQuery = taskService.createTaskQuery()
      .taskName("newName")
      .taskOwner("newOwner");
    Filter newFilter = filter.extend(extendingQuery);
    assertNull(newFilter.getId());

    TaskQueryImpl filterQuery = newFilter.getQuery();
    assertEquals("newName", filterQuery.getName());
    assertEquals("newOwner", filterQuery.getOwner());
  }

  public void testQueryFilter() {

    Filter filter2 = filterService.createTaskFilterQuery()
      .filterId(filter.getId())
      .filterName("name")
      .filterOwner("owner")
      .singleResult();

    compareFilter(filter, filter2);

    filter2 = filterService.createTaskFilterQuery()
      .filterNameLike("%m%")
      .singleResult();

    compareFilter(filter, filter2);
  }

  public void testQueryUnknownFilter() {
    Filter unknownFilter = filterService.createTaskFilterQuery()
      .filterId("unknown")
      .singleResult();

    assertNull(unknownFilter);

    unknownFilter = filterService.createTaskFilterQuery()
      .filterId(filter.getId())
      .filterName("invalid")
      .singleResult();

    assertNull(unknownFilter);
  }

  public void testDeleteFilter() {
    filterService.deleteFilter(filter.getId());

    filter = filterService.getFilter(filter.getId());
    assertNull(filter);
  }

  public void testDeleteUnknownFilter() {
    filterService.deleteFilter(filter.getId());
    long count = filterService.createFilterQuery().count();
    assertEquals(0, count);

    try {
      filterService.deleteFilter(filter.getId());
      fail("Exception expected");
    }
    catch (ProcessEngineException e) {
      // expected
    }
  }

  public static void compareFilter(Filter filter1, Filter filter2) {
    assertNotNull(filter1);
    assertNotNull(filter2);
    assertEquals(filter1.getId(), filter2.getId());
    assertEquals(filter1.getResourceType(), filter2.getResourceType());
    assertEquals(filter1.getName(), filter2.getName());
    assertEquals(filter1.getOwner(), filter2.getOwner());
    assertEquals(((FilterEntity) filter1).getQueryInternal(), ((FilterEntity) filter2).getQueryInternal());
    assertEquals(filter1.getProperties(), filter2.getProperties());
  }

}
