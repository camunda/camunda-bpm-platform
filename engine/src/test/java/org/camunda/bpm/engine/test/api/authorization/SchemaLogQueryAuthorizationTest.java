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
package org.camunda.bpm.engine.test.api.authorization;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.junit.After;
import org.junit.Test;

/**
 * @author Miklas Boskamp
 *
 */
public class SchemaLogQueryAuthorizationTest extends AuthorizationTest {

  @After
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void testSimpleQueryWithoutAuthorization() {
    // given

    // then
    assertThat(managementService.createSchemaLogQuery().list().size(), is(0));
  }

  @Test
  public void testCountQueryWithAuthorization() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    // then
    assertThat(managementService.createSchemaLogQuery().count(), is(greaterThan(0L)));
  }

  @Test
  public void testQueryWithAuthorization() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    // then
    assertThat(managementService.createSchemaLogQuery().list().size(), is(greaterThan(0)));
  }
}
