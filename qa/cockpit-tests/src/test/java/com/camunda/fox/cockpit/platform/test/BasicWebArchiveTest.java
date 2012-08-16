package com.camunda.fox.cockpit.platform.test;

import javax.inject.Inject;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.test.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.cdi.FoxProcessEngineLookup;
import com.camunda.fox.cdi.ProgrammaticBeanLookup;
import com.camunda.fox.cockpit.test.CockpitTestBase;

import static org.junit.Assert.*;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
@RunWith(Arquillian.class)
public class BasicWebArchiveTest extends CockpitTestBase {
  
  public static final int ONE_MINUTE = 60000;
  
  @org.jboss.arquillian.container.test.api.Deployment
  public static Archive<?> createApplicationDeployment() {
    WebArchive archive = createBaseDeployment();
    return archive;
  }
  
  @Inject
  private FoxProcessEngineLookup engineLookup;

  @Inject
  private ProcessEngine processEngine;
  
  @Inject
  private ProcessEngineConfigurationImpl processEngineConfiguration;
  
  @Test
  public void testProgrammaticBeanLookup() throws Exception {
    assertNotNull(processEngine);
    assertNotNull(processEngineConfiguration);
    assertNotNull(engineLookup);
    
    assertNotNull(lookup(ProcessEngine.class));
    assertNotNull(lookup(FoxProcessEngineLookup.class));
    assertNotNull(lookup(ProcessEngineConfigurationImpl.class));
  } 
  
  @Test
  @Deployment(resources = {
    "models/ParallelExecution.bpmn20.xml",
    "models/MyProcess.bpmn20.xml",
    "models/BusinessProcessBeanTest.test.bpmn20.xml",
    "models/SimpleProcurementExample.bpmn20.xml",
    "models/ImmediatelyFinishing.bpmn20.xml",
    "models/ImmediatelyFailing.bpmn20.xml"
  })
  public void testInjection() throws Exception {
    assertEquals(1, processEngine.getRepositoryService().createDeploymentQuery().count());
    assertEquals(6, processEngine.getRepositoryService().createProcessDefinitionQuery().count());
  }
  
  private <T> T lookup(Class<T> cls) {
    return ProgrammaticBeanLookup.lookup(cls);
  }
}