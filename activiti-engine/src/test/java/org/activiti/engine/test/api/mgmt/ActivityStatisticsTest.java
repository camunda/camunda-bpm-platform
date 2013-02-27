package org.activiti.engine.test.api.mgmt;

import java.util.List;

import junit.framework.Assert;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.ActivityStatistics;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

public class ActivityStatisticsTest extends PluggableActivitiTestCase {

  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testProcessDefinitionStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testActivityStatisticsQueryWithoutFailedJobs() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityRuntimeStatisticsQuery(definition.getId()).list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(1, activityResult.getInstances());
    Assert.assertEquals("theServiceTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testProcessDefinitionStatisticsQuery.bpmn20.xml")
  public void testActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityRuntimeStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(1, activityResult.getInstances());
    Assert.assertEquals("theTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testProcessDefinitionStatisticsQuery.bpmn20.xml")
  public void testManyInstancesActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityRuntimeStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(3, activityResult.getInstances());
    Assert.assertEquals("theTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testMultiInstanceActivityStatisticsQuery.bpmn20.xml")
  public void testMultiInstanceActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    
    List<ActivityStatistics> statistics = 
        managementService.createActivityRuntimeStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(4, activityResult.getInstances());
    Assert.assertEquals("theTask", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testSubprocessProcessDefinitionStatistics.bpmn20.xml")
  public void testSubprocessActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    List<ActivityStatistics> statistics = 
        managementService.createActivityRuntimeStatisticsQuery(definition.getId()).list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
    Assert.assertEquals("subProcessTask", result.getId());
  }
  
  @Test
  @Deployment(resources = {
      "org/activiti/engine/test/api/mgmt/StatisticsTest.testCallActivityProcessDefinitionStatisticsQuery.bpmn20.xml",
      "org/activiti/engine/test/api/mgmt/StatisticsTest.testProcessDefinitionStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testCallActivityActivityStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ExampleProcess").singleResult();
    List<ActivityStatistics> statistics = 
        managementService.createActivityRuntimeStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
    Assert.assertEquals(1, result.getFailedJobs());
    
    ProcessDefinition callSubProcessDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("callExampleSubProcess").singleResult();
    List<ActivityStatistics> callSubProcessStatistics = 
        managementService.createActivityRuntimeStatisticsQuery(callSubProcessDefinition.getId()).includeFailedJobs().list();
    
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
        managementService.createActivityRuntimeStatisticsQuery(definition.getId()).includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ActivityStatistics activityResult = statistics.get(0);
    Assert.assertEquals(1, activityResult.getInstances());
    Assert.assertEquals("theTimer", activityResult.getId());
    Assert.assertEquals(0, activityResult.getFailedJobs());
  }
}
