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

package org.camunda.bpm.engine.test.api.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;

public class MultiTenancyEventSubscriptionQueryTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String BPMN = "org/camunda/bpm/engine/test/api/multitenancy/messageStartEvent.bpmn";

  @Override
  protected void setUp() {
    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_ONE)
        .addClasspathResource(BPMN));

    deployment(repositoryService.createDeployment()
        .tenantId(TENANT_TWO)
        .addClasspathResource(BPMN));

    // the deployed process definition contains a message start event
    // - so a message event subscription is created on deployment.
  }

  public void testQueryWithoutTenantId() {
    EventSubscriptionQuery query = runtimeService.
        createEventSubscriptionQuery();

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantId() {
    EventSubscriptionQuery query = runtimeService.
        createEventSubscriptionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = runtimeService
        .createEventSubscriptionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    EventSubscriptionQuery query = runtimeService.
        createEventSubscriptionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByNonExistingTenantId() {
    EventSubscriptionQuery query = runtimeService.
        createEventSubscriptionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      runtimeService.createEventSubscriptionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(eventSubscriptions.size(), is(2));
    assertThat(eventSubscriptions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(eventSubscriptions.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(eventSubscriptions.size(), is(2));
    assertThat(eventSubscriptions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(eventSubscriptions.get(1).getTenantId(), is(TENANT_ONE));
  }

}
