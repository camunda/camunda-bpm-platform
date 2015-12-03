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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Groups.CAMUNDA_ADMIN;

import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.management.TableMetaData;
import org.camunda.bpm.engine.management.TablePage;

/**
 * @author Roman Smirnov
 *
 */
public class ManagementAuthorizationTest extends AuthorizationTest {

  // get table count //////////////////////////////////////////////

  public void testGetTableCountWithoutAuthorization() {
    // given

    try {
      // when
      managementService.getTableCount();
      fail("Exception expected: It should not be possible to get the table count");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent("ENGINE-03029 The user with id 'test' is not a member of the group with id 'camunda-admin'", message);
    }
  }

  public void testGetTableCountAsCamundaAdmin() {
    // given
    createGroup(CAMUNDA_ADMIN);
    createMembership(userId, CAMUNDA_ADMIN);

    // when
    Map<String, Long> tableCount = managementService.getTableCount();

    // then
    assertFalse(tableCount.isEmpty());
  }

  // get table name //////////////////////////////////////////////

  public void testGetTableNameWithoutAuthorization() {
    // given

    try {
      // when
      managementService.getTableName(ProcessDefinitionEntity.class);
      fail("Exception expected: It should not be possible to get the table name");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent("ENGINE-03029 The user with id 'test' is not a member of the group with id 'camunda-admin'", message);
    }
  }

  public void testGetTableNameAsCamundaAdmin() {
    // given
    createGroup(CAMUNDA_ADMIN);
    createMembership(userId, CAMUNDA_ADMIN);

    // when
    String tableName = managementService.getTableName(ProcessDefinitionEntity.class);

    // then
    assertEquals("ACT_RE_PROCDEF", tableName);
  }

  // get table meta data //////////////////////////////////////////////

  public void testGetTableMetaDataWithoutAuthorization() {
    // given

    try {
      // when
      managementService.getTableMetaData("ACT_RE_PROCDEF");
      fail("Exception expected: It should not be possible to get the table meta data");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent("ENGINE-03029 The user with id 'test' is not a member of the group with id 'camunda-admin'", message);
    }
  }

  public void testGetTableMetaDataAsCamundaAdmin() {
    // given
    createGroup(CAMUNDA_ADMIN);
    createMembership(userId, CAMUNDA_ADMIN);

    // when
    TableMetaData tableMetaData = managementService.getTableMetaData("ACT_RE_PROCDEF");

    // then
    assertNotNull(tableMetaData);
  }

  // table page query //////////////////////////////////

  public void testTablePageQueryWithoutAuthorization() {
    // given

    try {
      // when
      managementService.createTablePageQuery().tableName("ACT_RE_PROCDEF").listPage(0, Integer.MAX_VALUE);
      fail("Exception expected: It should not be possible to get a table page");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent("ENGINE-03029 The user with id 'test' is not a member of the group with id 'camunda-admin'", message);
    }

  }

  public void testTablePageQueryAsCamundaAdmin() {
    // given
    createGroup(CAMUNDA_ADMIN);
    createMembership(userId, CAMUNDA_ADMIN);

    // when
    TablePage page = managementService.createTablePageQuery().tableName("ACT_RE_PROCDEF").listPage(0, Integer.MAX_VALUE);

    // then
    assertNotNull(page);
  }

  // get history level /////////////////////////////////

  public void testGetHistoryLevelWithoutAuthorization() {
    //given

    try {
      // when
      managementService.getHistoryLevel();
      fail("Exception expected: It should not be possible to get the history level");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent("ENGINE-03029 The user with id 'test' is not a member of the group with id 'camunda-admin'", message);
    }
  }

  public void testGetHistoryLevelAsCamundaAdmin() {
    //given
    createGroup(CAMUNDA_ADMIN);
    createMembership(userId, CAMUNDA_ADMIN);

    // when
    int historyLevel = managementService.getHistoryLevel();

    // then
    assertEquals(processEngineConfiguration.getHistoryLevel().getId(), historyLevel);
  }

  // database schema upgrade ///////////////////////////

  public void testDataSchemaUpgradeWithoutAuthorization() {
    // given

    try {
      // when
      managementService.databaseSchemaUpgrade(null, null, null);
      fail("Exception expected: It should not be possible to upgrade the database schema");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent("ENGINE-03029 The user with id 'test' is not a member of the group with id 'camunda-admin'", message);
    }
  }

  // helper ////////////////////////////////////////////

  protected Group createGroup(String groupId) {
    disableAuthorization();
    Group group = super.createGroup(groupId);
    enableAuthorization();
    return group;
  }

  protected void createMembership(String userId, String groupId) {
    disableAuthorization();
    identityService.createMembership(userId, groupId);
    enableAuthorization();
  }

}
