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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;

public class MultiTenancyCreateCaseInstanceTest extends PluggableProcessEngineTestCase {

  protected static final String CMMN_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected static final String TENANT_ONE = "tenant1";

  public void testFailToCreateCaseInstanceByIdWithoutTenantId() {
    deployment(CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();

    try {
      caseService.withCaseDefinition(caseDefinition.getId())
          .caseDefinitionWithoutTenantId()
          .create();
      fail("BadUserRequestException exception");
    } catch(BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot specify a tenant-id"));
    }
  }

  public void testFailToCreateCaseInstanceByIdWithTenantId() {
    deploymentForTenant(TENANT_ONE, CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();

    try {
      caseService.withCaseDefinition(caseDefinition.getId())
          .caseDefinitionTenantId(TENANT_ONE)
          .create();
      fail("BadUserRequestException exception");
    } catch(BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot specify a tenant-id"));
    }
  }

}
