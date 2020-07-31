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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.management.TablePage;
import org.junit.Test;


/**
 * @author Roman Smirnov
 *
 */
public class ManagementAuthorizationTest extends AuthorizationTest {

  private static final String REQUIRED_ADMIN_AUTH_EXCEPTION = "ENGINE-03029 Required admin authenticated group or user.";

  // get table count //////////////////////////////////////////////

  @Test
  public void testGetTableCountWithoutAuthorization() {
    // given

    try {
      // when
      managementService.getTableCount();
      fail("Exception expected: It should not be possible to get the table count");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }
  }

  @Test
  public void testGetTableCountAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    // when
    Map<String, Long> tableCount = managementService.getTableCount();

    // then
    assertFalse(tableCount.isEmpty());
  }

  // get table name //////////////////////////////////////////////

  @Test
  public void testGetTableNameWithoutAuthorization() {
    // given

    try {
      // when
      managementService.getTableName(ProcessDefinitionEntity.class);
      fail("Exception expected: It should not be possible to get the table name");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }
  }

  @Test
  public void testGetTableNameAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    // when
    String tableName = managementService.getTableName(ProcessDefinitionEntity.class);

    // then
    assertEquals(tablePrefix + "ACT_RE_PROCDEF", tableName);
  }

  // get table meta data //////////////////////////////////////////////

  @Test
  public void testGetTableMetaDataWithoutAuthorization() {
    // given

    try {
      // when
      managementService.getTableMetaData("ACT_RE_PROCDEF");
      fail("Exception expected: It should not be possible to get the table meta data");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }
  }

  @Test
  public void testGetTableMetaDataAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    // when
    TableMetaData tableMetaData = managementService.getTableMetaData("ACT_RE_PROCDEF");

    // then
    assertNotNull(tableMetaData);
  }

  // table page query //////////////////////////////////

  @Test
  public void testTablePageQueryWithoutAuthorization() {
    // given

    try {
      // when
      managementService.createTablePageQuery().tableName("ACT_RE_PROCDEF").listPage(0, Integer.MAX_VALUE);
      fail("Exception expected: It should not be possible to get a table page");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }

  }

  @Test
  public void testTablePageQueryAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

    // when
    TablePage page = managementService.createTablePageQuery().tableName(tablePrefix + "ACT_RE_PROCDEF").listPage(0, Integer.MAX_VALUE);

    // then
    assertNotNull(page);
  }

  // get history level /////////////////////////////////

  @Test
  public void testGetHistoryLevelWithoutAuthorization() {
    //given

    try {
      // when
      managementService.getHistoryLevel();
      fail("Exception expected: It should not be possible to get the history level");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }
  }

  @Test
  public void testGetHistoryLevelAsCamundaAdmin() {
    //given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    // when
    int historyLevel = managementService.getHistoryLevel();

    // then
    assertEquals(processEngineConfiguration.getHistoryLevel().getId(), historyLevel);
  }

  // database schema upgrade ///////////////////////////

  @Test
  public void testDataSchemaUpgradeWithoutAuthorization() {
    // given

    try {
      // when
      managementService.databaseSchemaUpgrade(null, null, null);
      fail("Exception expected: It should not be possible to upgrade the database schema");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }
  }

  // get properties & set/delete property ///////////////////////////

  @Test
  public void testGetPropertiesWithoutAuthorization() {
    // given

    try {
      // when
      managementService.getProperties();
      fail("Exception expected: It should not be possible to get properties");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }
  }

  @Test
  public void testSetPropertyWithoutAuthorization() {
    // given

    try {
      // when
      managementService.setProperty("aPropertyKey", "aPropertyValue");
      fail("Exception expected: It should not be possible to set a property");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }
  }

  @Test
  public void testDeletePropertyWithoutAuthorization() {
    // given

    try {
      // when
      managementService.deleteProperty("aPropertyName");
      fail("Exception expected: It should not be possible to delete a property");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }
  }

  // configure telemetry /////////////////////////////////////

  @Test
  public void testTelemetryEnabledWithoutAutorization() {
    // given

    try {
      // when
      managementService.toggleTelemetry(false);
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);
    }
  }

  @Test
  public void testTelemetryEnabledAsCamundaAdmin() {
    // given
    disableAuthorization();
    managementService.toggleTelemetry(true);
    enableAuthorization();
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    // when
    managementService.toggleTelemetry(false);

    // then
    assertThat(managementService.isTelemetryEnabled()).isFalse();
  }

}
