package org.activiti.engine.test.api.mgmt;

import java.util.List;

import junit.framework.Assert;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.DeploymentStatistics;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;

public class DeploymentStatisticsQueryTest extends PluggableActivitiTestCase {

  @Test
  public void testDeploymentStatisticsQuery() {
    String deploymentName = "my deployment";
    
    Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
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
    Assert.assertEquals(deployment.getDeploymentTime(), result.getDeploymentTime());
    
    repositoryService.deleteDeployment(deployment.getId(), true);
  }
  
  @Test
  public void testDeploymentStatisticsQueryCountAndPaging() {
    Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
        .deploy();
    
    runtimeService.startProcessInstanceByKey("MIExampleProcess");
    runtimeService.startProcessInstanceByKey("ParGatewayExampleProcess");
    
    Deployment anotherDeployment = repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
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
  public void testDeploymentStatisticsQueryWithoutRunningInstances() {
    
    Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testMultiInstanceStatisticsQuery.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/api/mgmt/StatisticsTest.testParallelGatewayStatisticsQuery.bpmn20.xml")
        .deploy();
    
    List<DeploymentStatistics> statistics = 
        managementService.createDeploymentStatisticsQuery().includeFailedJobs().list();
    
    Assert.assertEquals(1, statistics.size());
    
    DeploymentStatistics result = statistics.get(0);
    Assert.assertEquals(0, result.getInstances());
    Assert.assertEquals(0, result.getFailedJobs());
    
    repositoryService.deleteDeployment(deployment.getId(), true);
  }
}
