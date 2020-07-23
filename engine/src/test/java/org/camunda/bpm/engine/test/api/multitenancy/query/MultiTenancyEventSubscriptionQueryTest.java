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
package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyEventSubscriptionQueryTest extends PluggableProcessEngineTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
        .message("start")
        .userTask()
        .endEvent()
        .done();

    deployment(process);
    deploymentForTenant(TENANT_ONE, process);
    deploymentForTenant(TENANT_TWO, process);

    // the deployed process definition contains a message start event
    // - so a message event subscription is created on deployment.
  }

  public void testQueryNoTenantIdSet() {
    EventSubscriptionQuery query = runtimeService
        .createEventSubscriptionQuery();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByTenantId() {
    EventSubscriptionQuery query = runtimeService
        .createEventSubscriptionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = runtimeService
        .createEventSubscriptionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    EventSubscriptionQuery query = runtimeService
        .createEventSubscriptionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryBySubscriptionsWithoutTenantId() {
    EventSubscriptionQuery query = runtimeService
        .createEventSubscriptionQuery()
        .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIdsIncludeSubscriptionsWithoutTenantId() {
    EventSubscriptionQuery query = runtimeService
        .createEventSubscriptionQuery()
        .tenantIdIn(TENANT_ONE)
        .includeEventSubscriptionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = runtimeService
        .createEventSubscriptionQuery()
        .tenantIdIn(TENANT_TWO)
        .includeEventSubscriptionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = runtimeService
        .createEventSubscriptionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeEventSubscriptionsWithoutTenantId();

    assertThat(query.count(), is(3L));
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
    // exclude subscriptions without tenant id because of database-specific ordering
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(eventSubscriptions.size(), is(2));
    assertThat(eventSubscriptions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(eventSubscriptions.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude subscriptions without tenant id because of database-specific ordering
    List<EventSubscription> eventSubscriptions = runtimeService.createEventSubscriptionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(eventSubscriptions.size(), is(2));
    assertThat(eventSubscriptions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(eventSubscriptions.get(1).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();
    assertThat(query.count(), is(1L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeEventSubscriptionsWithoutTenantId().count(), is(2L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();
    assertThat(query.count(), is(3L));
  }

}
