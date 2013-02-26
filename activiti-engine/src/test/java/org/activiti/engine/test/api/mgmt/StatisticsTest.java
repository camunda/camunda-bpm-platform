package org.activiti.engine.test.api.mgmt;

import java.util.List;

import junit.framework.Assert;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.ActivityStatistics;
import org.activiti.engine.management.ProcessDefinitionStatistics;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;
import org.junit.Test;


public class StatisticsTest extends PluggableActivitiTestCase {

  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testProcessDefinitionStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryWithFailedJobs() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");

    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionRuntimeStatisticsQuery().includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(1, definitionResult.getInstances());
    Assert.assertEquals(1, definitionResult.getFailedJobs());
  }
  
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
    Assert.assertEquals("theTask", activityResult.getId());
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
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testMultiInstanceActivityStatisticsQuery.bpmn20.xml")
  public void testMultiInstanceActivityStatisticsQuery() {
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
}
