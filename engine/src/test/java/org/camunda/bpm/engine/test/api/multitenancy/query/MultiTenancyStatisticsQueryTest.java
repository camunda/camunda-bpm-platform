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

package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class MultiTenancyStatisticsQueryTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    BpmnModelInstance process = Bpmn.createExecutableProcess()
      .startEvent().done();

    deployment(process);
    deploymentForTenant(TENANT_ONE, process);
    deploymentForTenant(TENANT_TWO, process);
  }

  public void testDeploymentStatistics() {
    List<DeploymentStatistics> deploymentStatistics = managementService
        .createDeploymentStatisticsQuery()
        .list();

    assertThat(deploymentStatistics.size(), is(3));
    assertThat(deploymentStatistics, hasItem(deploymentWithTenantId(null)));
    assertThat(deploymentStatistics, hasItem(deploymentWithTenantId(TENANT_ONE)));
    assertThat(deploymentStatistics, hasItem(deploymentWithTenantId(TENANT_TWO)));
  }

  public void testProcessDefinitionStatistics() {
    List<ProcessDefinitionStatistics> processDefinitionStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();

    assertThat(processDefinitionStatistics.size(), is(3));
    assertThat(processDefinitionStatistics, hasItem(processDefinitionWithTenantId(null)));
    assertThat(processDefinitionStatistics, hasItem(processDefinitionWithTenantId(TENANT_ONE)));
    assertThat(processDefinitionStatistics, hasItem(processDefinitionWithTenantId(TENANT_TWO)));
  }

  protected Matcher<DeploymentStatistics> deploymentWithTenantId(final String tenantId) {
    return new TypeSafeMatcher<DeploymentStatistics>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("a deployment with tenant-id ").appendValue(tenantId);
      }

      @Override
      protected boolean matchesSafely(DeploymentStatistics item) {
        if(tenantId == null) {
          return item.getTenantId() == null;
        } else {
          return tenantId.equals(item.getTenantId());
        }
      }
    };
  }

  protected Matcher<ProcessDefinitionStatistics> processDefinitionWithTenantId(final String tenantId) {
    return new TypeSafeMatcher<ProcessDefinitionStatistics>() {

      @Override
      public void describeTo(Description description) {
        description.appendText("a process definition with tenant-id ").appendValue(tenantId);
      }

      @Override
      protected boolean matchesSafely(ProcessDefinitionStatistics item) {
        if(tenantId == null) {
          return item.getTenantId() == null;
        } else {
          return tenantId.equals(item.getTenantId());
        }
      }
    };
  }

}
