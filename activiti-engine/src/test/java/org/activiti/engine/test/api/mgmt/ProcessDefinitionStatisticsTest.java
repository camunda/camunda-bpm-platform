package org.activiti.engine.test.api.mgmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.ProcessDefinitionStatistics;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;
import org.junit.Test;


public class ProcessDefinitionStatisticsTest extends PluggableActivitiTestCase {

  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryWithFailedJobs() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("fail", false);
    runtimeService.startProcessInstanceByKey("ExampleProcess", parameters);

    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionStatisticsQuery().includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(2, definitionResult.getInstances());
    Assert.assertEquals(1, definitionResult.getFailedJobs());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryCount() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    long count = 
        managementService.createProcessDefinitionStatisticsQuery().includeFailedJobs().count();
    
    Assert.assertEquals(1, count);
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
  public void testMultiInstanceProcessDefinitionStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionStatisticsQuery().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ProcessDefinitionStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testSubprocessStatisticsQuery.bpmn20.xml")
  public void testSubprocessProcessDefinitionStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("ExampleProcess");
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionStatisticsQuery().list();
    
    Assert.assertEquals(1, statistics.size());
    
    ProcessDefinitionStatistics result = statistics.get(0);
    Assert.assertEquals(1, result.getInstances());
  }
  
  @Test
  @Deployment(resources = {
      "org/activiti/engine/test/api/mgmt/StatisticsTest.testCallActivityStatisticsQuery.bpmn20.xml",
      "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml"})
  public void testCallActivityProcessDefinitionStatisticsQuery() {
    runtimeService.startProcessInstanceByKey("callExampleSubProcess");
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionStatisticsQuery().includeFailedJobs().list();
    
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
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryForMultipleVersions() {
    org.activiti.engine.repository.Deployment deployment = 
        repositoryService.createDeployment()
          .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
          .deploy();
    
    List<ProcessDefinition> definitions = 
        repositoryService.createProcessDefinitionQuery().processDefinitionKey("ExampleProcess").list();
    
    for (ProcessDefinition definition : definitions) {
      runtimeService.startProcessInstanceById(definition.getId());
    }
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionStatisticsQuery().includeFailedJobs().list();
    
    Assert.assertEquals(2, statistics.size());
    
    ProcessDefinitionStatistics definitionResult = statistics.get(0);
    Assert.assertEquals(1, definitionResult.getInstances());
    Assert.assertEquals(1, definitionResult.getFailedJobs());
    
    repositoryService.deleteDeployment(deployment.getId(), true);
  }
  
  @Test
  @Deployment(resources = "org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQueryWithFailedJobs.bpmn20.xml")
  public void testProcessDefinitionStatisticsQueryPagination() {
    org.activiti.engine.repository.Deployment deployment = 
        repositoryService.createDeployment()
          .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testStatisticsQuery.bpmn20.xml")
          .deploy();
    
    List<ProcessDefinition> definitions = 
        repositoryService.createProcessDefinitionQuery().processDefinitionKey("ExampleProcess").list();
    
    for (ProcessDefinition definition : definitions) {
      runtimeService.startProcessInstanceById(definition.getId());
    }
    
    List<ProcessDefinitionStatistics> statistics = 
        managementService.createProcessDefinitionStatisticsQuery().includeFailedJobs().listPage(0, 1);
    
    Assert.assertEquals(1, statistics.size());
    
    repositoryService.deleteDeployment(deployment.getId(), true);
  }
  
}
