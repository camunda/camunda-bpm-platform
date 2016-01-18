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

import java.util.List;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.test.api.repository.AbstractDefinitionQueryTest;

public class MultiTenancyProcessDefinitionQueryTest extends AbstractDefinitionQueryTest {

  protected static final String TENANT_ONE = "tenantOne";
  protected static final String TENANT_TWO = "tenantTwo";

  @Override
  protected String getResourceOnePath() {
    return "org/camunda/bpm/engine/test/repository/one.bpmn20.xml";
  }

  @Override
  protected String getResourceTwoPath() {
    return "org/camunda/bpm/engine/test/repository/two.bpmn20.xml";
  }

  @Override
  protected String getTenantOne() {
    return TENANT_ONE;
  }

  @Override
  protected String getTenantTwo() {
    return TENANT_TWO;
  }

  public void testQueryWithoutTenantId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
    verifyQueryResults(query, 3);
  }

  public void testQueryByTenantId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().tenantId(TENANT_ONE);
    verifyQueryResults(query, 2);

    query = repositoryService.createProcessDefinitionQuery().tenantId(TENANT_TWO);
    verifyQueryResults(query, 1);
  }

  public void testQueryByTenantIds() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().tenantIdIn(TENANT_ONE, TENANT_TWO);
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().tenantIdIn(TENANT_ONE);
    verifyQueryResults(query, 2);
  }

  public void testQueryByKey() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one");
    // one definition for each tenant
    verifyQueryResults(query, 2);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").tenantId(TENANT_ONE);
    verifyQueryResults(query, 1);
  }

  public void testQueryByLatest() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").latestVersion();
    // one definition for each tenant
    verifyQueryResults(query, 2);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").tenantId(TENANT_ONE).latestVersion();
    verifyQueryResults(query, 1);
  }

  public void testQuerySorting() {
    // asc
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().orderByTenantId().asc();
    verifyQueryResults(query, 3);

    // desc
    query = repositoryService.createProcessDefinitionQuery().orderByTenantId().desc();
    verifyQueryResults(query, 3);

    // Typical use case
    query = repositoryService.createProcessDefinitionQuery().orderByTenantId().asc().orderByProcessDefinitionKey().asc();
    List<ProcessDefinition> processDefinitions = query.list();
    assertEquals(3, processDefinitions.size());

    assertEquals("one", processDefinitions.get(0).getKey());
    assertEquals(TENANT_ONE, processDefinitions.get(0).getTenantId());
    assertEquals("two", processDefinitions.get(1).getKey());
    assertEquals(TENANT_ONE, processDefinitions.get(1).getTenantId());
    assertEquals("one", processDefinitions.get(2).getKey());
    assertEquals(TENANT_TWO, processDefinitions.get(2).getTenantId());
  }

}
