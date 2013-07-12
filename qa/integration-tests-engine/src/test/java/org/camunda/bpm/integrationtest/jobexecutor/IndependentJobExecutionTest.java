package org.camunda.bpm.integrationtest.jobexecutor;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class IndependentJobExecutionTest extends AbstractFoxPlatformIntegrationTest {
  
  private ProcessEngine engine2;
  private ProcessEngineConfigurationImpl engine2Configuration;
  
  private ProcessEngine engine3;
  private ProcessEngineConfigurationImpl engine3Configuration;
  
  @Before
  public void setEngines() {
    ProcessEngineService engineService = BpmPlatform.getProcessEngineService();
    engine2 = engineService.getProcessEngine("engine2");
    engine3 = engineService.getProcessEngine("engine3");
    engine2Configuration = ((ProcessEngineImpl) engine2).getProcessEngineConfiguration();
    engine3Configuration = ((ProcessEngineImpl) engine2).getProcessEngineConfiguration();
  }
//  
//  @Deployment(order = 0, name="engines")
//  public static WebArchive enginesArchive() {    
//    
//    return initWebArchiveDeployment("engines.war")
//          .addAsLibraries(
//              ShrinkWrap.create(JavaArchive.class, "archives.jar")
//              .addAsResource("twoEngines.xml", "META-INF/processes.xml"));
//            
//  }
  
  @Deployment(order = 1, name="archives")
  public static WebArchive processArchive() {    
    
    return initWebArchiveDeployment("archives.war")
            .addAsLibraries(
                ShrinkWrap.create(JavaArchive.class, "engines.jar")
                  .addAsResource("twoEngines.xml", "META-INF/processes.xml"),
                ShrinkWrap.create(JavaArchive.class, "archives.jar")
                  .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/IndependentArchives.xml", "META-INF/processes.xml")
                  .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/archive1process.bpmn20.xml")
                  .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/archive2process.bpmn20.xml"));
  }
  
  @Test
  public void testIndependentJobExecution() {
    JobExecutor jobExecutor2 = engine2Configuration.getJobExecutor();
    JobExecutor jobExecutor3 = engine3Configuration.getJobExecutor();
    
    ProcessInstance instance1 = engine2.getRuntimeService().startProcessInstanceByKey("archive1Process");
    ProcessInstance instance2 = engine3.getRuntimeService().startProcessInstanceByKey("archive2Process");
    
    waitForJobExecutorToProcessAllJobs(jobExecutor2, 5000L);
    Job remainingJob = engine3.getManagementService().createJobQuery().singleResult();
    Assert.assertNotNull("Should not have processed job that belongs to a deployment of engine3", remainingJob);
    Assert.assertEquals(instance1.getId(), remainingJob.getProcessInstanceId());
    
    waitForJobExecutorToProcessAllJobs(jobExecutor3, 5000L);
    long remainingJobsCount = engine3.getManagementService().createJobQuery().count();
    Assert.assertEquals("Should have processed the remaining job", 0, remainingJobsCount);
    
  }
}
