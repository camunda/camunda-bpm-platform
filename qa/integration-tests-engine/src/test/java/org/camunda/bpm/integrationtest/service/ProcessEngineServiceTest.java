package org.camunda.bpm.integrationtest.service;

import org.junit.Assert;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thorben Lindhauer
 */
@RunWith(Arquillian.class)
public class ProcessEngineServiceTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment(name="test1")
  public static WebArchive app1() {    
    return initWebArchiveDeployment("test1.war");
  }
  
  @Test
  @OperateOnDeployment("test1")
  public void testNonExistingEngineRetrieval() {
    
    ProcessEngineService engineService = BpmPlatform.getProcessEngineService();
    ProcessEngine engine = engineService.getProcessEngine("aNonExistingEngineName");
    Assert.assertNull(engine);
  }
}
