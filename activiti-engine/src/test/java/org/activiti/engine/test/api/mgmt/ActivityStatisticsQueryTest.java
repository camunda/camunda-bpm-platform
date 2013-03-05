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

package org.activiti.engine.test.api.mgmt;

import java.util.List;

import junit.framework.Assert;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.ActivityStatistics;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

public class ActivityStatisticsQueryTest extends PluggableActivitiTestCase {
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testActivityStatisticsQueryWithoutFailedJobs() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(1, activityResult.getInstances());
    Assert.assertEquals("theServiceTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
  public void testActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(1, activityResult.getInstances());
    Assert.assertEquals("theTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
  public void testActivityStatisticsQueryCount() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    long count = 
        managementService.createActivityStatisticsQuery(definition.getId()).includeFailedJobs().count();
    
    Assert.assertEquals(1, count);
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
  public void testManyInstancesActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(3, activityResult.getInstances());
    Assert.assertEquals("theTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
  public void failing_testMultiInstanceActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(3, activityResult.getInstances());
    Assert.assertEquals("theTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testSubprocessStatisticsQuery.bpmn20.xml")
  public void testSubprocessActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
    Assert.assertEquals("subProcessTask", result.getId());
  }
  
  @Test
  @Deployment(resources = {
      "org/activiti/engine/test/api/mgmt/StatisticsTest.testCallActivityStatisticsQuery.bpmn20.xml",
      "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testCallActivityActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
    Assert.assertEquals(1, result.getFailedJobs());
    
    ProcessDefinition callSubProcessDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("callExampleSubProcess").singleResult();
    List<ActivityStatistics> callSubProcessStatistics = 
        managementService.createActivityStatisticsQuery(callSubProcessDefinition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, callSubProcessStatistics.size());
    
    result = callSubProcessStatistics.get(0);
    Assert.assertEquals(1, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testActivityStatisticsQueryWithIntermediateTimer.bpmn20.xml")
  public void testActivityStatisticsQueryWithIntermediateTimer() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(1, activityResult.getInstances());
    Assert.assertEquals("theTimer", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
  
  @Test
  public void testNullProcessDefinitionParameter() {
    try {
      managementService.createActivityStatisticsQuery(null).list();
      Assert.fail();
    } catch (ActivitiException e) {
      // expected
    }
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
  public void testActivityStatisticsQueryPagination() {
    
    ProcessDefinition definition = 
        repositoryService.createProcessDefinitionQuery().processDefinitionKey("ParGatewayExampleProcess").singleResult();
    runtimeService.startProcessInstanceById(definition.getId());
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).includeFailedJobs().listPage(0, 1);
    
    Assert.assertEquals(1, statistics.size());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
  public void testParallelGatewayActivityStatisticsQuery() {
    
    ProcessDefinition definition = 
        repositoryService.createProcessDefinitionQuery().processDefinitionKey("ParGatewayExampleProcess").singleResult();
    runtimeService.startProcessInstanceById(definition.getId());
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).list();
    
    Assert.assertEquals(2, statistics.size());
    
    for (ActivityStatistics result : statistics) {
      Assert.assertEquals(1, result.getInstances());
    }
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
  public void failing_testActivityStatisticsQueryWithNoInstances() {
    
    ProcessDefinition definition = 
        repositoryService.createProcessDefinitionQuery().processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityStatisticsQuery(definition.getId()).list();
    
    Assert.assertEquals(1, statistics.size());
    ActivityStatistics result = statistics.get(0);
    Assert.assertEquals("theTask", result.getId());
    Assert.assertEquals(0, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());
    
  }
}
