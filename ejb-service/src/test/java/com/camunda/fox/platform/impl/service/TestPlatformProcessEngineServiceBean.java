package com.camunda.fox.platform.impl.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.EJB;
import javax.naming.NamingException;

import org.activiti.engine.ProcessEngine;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.api.ProcessEngineService;
import com.camunda.fox.platform.api.ProcessEngineService.ProcessEngineStartOperation;
import com.camunda.fox.platform.impl.test.DummyProcessEngineConfiguration;
import com.camunda.fox.platform.impl.test.H2Datasource;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * 
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class TestPlatformProcessEngineServiceBean {

  @Deployment
  public static WebArchive createDeployment() {
    return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClass(H2Datasource.class)
            .addClass(PlatformServiceBean.class)
            .addClass(DummyProcessEngineConfiguration.class)            
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");            
  }
  
  @EJB
  private ProcessEngineService processEngineService;
    
  @Test
  public void testProcessEngineServiceEjb() throws NamingException, InterruptedException, ExecutionException {
    ProcessEngineConfiguration configuration = new DummyProcessEngineConfiguration(true, "default", "java:global/test/FoxEngine", "audit", true, false);
    
    Assert.assertEquals(0, processEngineService.getProcessEngines().size());
    
    Future<ProcessEngineStartOperation> startProcessEngine = processEngineService.startProcessEngine(configuration);
    ProcessEngineStartOperation processEngineStartOperation = startProcessEngine.get();
    ProcessEngine processEngine = processEngineStartOperation.getProcessenEngine();
    
    Assert.assertEquals(1, processEngineService.getProcessEngines().size());
    Assert.assertEquals(processEngine, processEngineService.getDefaultProcessEngine());
    
    processEngineService.stopProcessEngine(processEngine);
    
    Assert.assertEquals(0, processEngineService.getProcessEngines().size());   
  }
    
  
}
