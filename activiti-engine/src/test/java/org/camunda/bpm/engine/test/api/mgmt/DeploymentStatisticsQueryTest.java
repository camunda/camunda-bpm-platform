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

package org.camunda.bpm.engine.test.api.mgmt;

import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.DeploymentStatistics;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

public class DeploymentStatisticsQueryTest extends PluggableProcessEngineTestCase {

  @Test
  public void testDeploymentStatisticsQuery() {
    String deploymentName = "my deployment";
    
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
        .name(deploymentName)
        .deploy();
    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ParGatewayExampleProcess");
    
    List<DeploymentStatistics> statistics = 
        managementService.createDeploymentStatisticsQuery().includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    DeploymentStatistics result = statistics.get(0);
    Assert.assertEquals(2, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());

    Assert.assertEquals(deployment.getId(), result.getId());
    Assert.assertEquals(deploymentName, result.getName());
    
    // only compare time on second level (i.e. drop milliseconds)
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(deployment.getDeploymentTime());
    cal1.set(Calendar.MILLISECOND, 0);
    
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(result.getDeploymentTime());
    cal2.set(Calendar.MILLISECOND, 0);
    
    Assert.assertTrue(cal1.equals(cal2));
    
    repositoryService.deleteDeployment(deployment.getId(), true);
  }
  
  @Test
  public void testDeploymentStatisticsQueryCountAndPaging() {
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
        .deploy();
    
    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ParGatewayExampleProcess");
    
    org.camunda.bpm.engine.repository.Deployment anotherDeployment = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
        .deploy();
    
    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ParGatewayExampleProcess");
    
    long count = managementService.createDeploymentStatisticsQuery().includeFailedJobs().count();
    
    Assert.assertEquals(2, count);
    
    List<DeploymentStatistics> statistics = managementService.createDeploymentStatisticsQuery().includeFailedJobs().listPage(0, 1);
    Assert.assertEquals(1, statistics.size());
    
    repositoryService.deleteDeployment(deployment.getId(), true);
    repositoryService.deleteDeployment(anotherDeployment.getId(), true);
  }
  
  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testDeploymentStatisticsQueryWithFailedJobs() {
    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    List<DeploymentStatistics> statistics = 
        managementService.createDeploymentStatisticsQuery().includeFailedJobs().list();
    
    DeploymentStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml"})
  public void testDeploymentStatisticsQueryWithoutRunningInstances() {
    List<DeploymentStatistics> statistics = 
        managementService.createDeploymentStatisticsQuery().includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    DeploymentStatistics result = statistics.get(0);
    Assert.assertEquals(0, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());
  }
}
