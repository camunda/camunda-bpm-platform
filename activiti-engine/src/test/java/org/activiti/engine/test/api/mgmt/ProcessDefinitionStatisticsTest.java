package org.activiti.engine.test.api.mgmt;

import java.util.List;

import junit.framework.Assert;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.ProcessDefinitionStatistics;
import org.activiti.engine.test.Deployment;
import org.junit.Test;


public class ProcessDefinitionStatisticsTest extends PluggableActivitiTestCase {

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
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testMultiInstanceActivityStatisticsQuery.bpmn20.xml")
  public void testMultiInstanceProcessDefinitionStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionRuntimeStatisticsQuery().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ProcessDefinitionStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testSubprocessProcessDefinitionStatistics.bpmn20.xml")
  public void testSubprocessProcessDefinitionStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionRuntimeStatisticsQuery().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ProcessDefinitionStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
  }
  
  @Test
  @Deployment(resources = {
      "org/activiti/engine/test/api/mgmt/StatisticsTest.testCallActivityProcessDefinitionStatisticsQuery.bpmn20.xml",
      "org/activiti/engine/test/api/mgmt/StatisticsTest.testProcessDefinitionStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testCallActivityProcessDefinitionStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionRuntimeStatisticsQuery().includeFailedJobs().list();
    
    Assert.assertEquals(2, statistics.size());
    
    for (ProcessDefinitionStatistics result : statistics) {
      if (result.getKey().equals("ExampleSubProcess")) {
        Assert.assertEquals(1, result.getInstances());
        Assert.assertEquals(1, result.getFailedJobs());
      } else if (result.getKey().equals("callExampleSubProcess")) {
        Assert.assertEquals(1, result.getInstances());
        Assert.assertEquals(0, result.getFailedJobs());
      }
    }
  }
  
}
