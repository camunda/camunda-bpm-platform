package com.camunda.fox.platform.impl.service;

import java.util.concurrent.Future;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.junit.Assert;
import org.junit.Test;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessEngineService.ProcessEngineStartOperation;
import com.camunda.fox.platform.impl.service.util.DummyProcessEngineConfiguration;

/**
 * 
 * @author Daniel Meyer
 */
public class TestProcessEngineService extends PlatformServiceTest {
  
  @Test
  public void testGetUnexistingProcessEngineFails() {
    try {
      processEngineService.getProcessEngine("unexistingProcessEngine");
      Assert.fail();
    }catch (FoxPlatformException e) {
      // expected
    }
  }
  
  @Test
  public void testStartProcessEngineWithNoNameFails() throws Exception {
    DummyProcessEngineConfiguration processEngineConfiguration = new DummyProcessEngineConfiguration(true, null, ENGINE_DS1, "audit", true, false);
    
    Assert.assertEquals(0, processEngineService.getProcessEngines().size());
    Assert.assertEquals(0, processEngineService.getProcessEngineNames().size());
    
    try {
      processEngineService.startProcessEngine(processEngineConfiguration);
      Assert.fail("exception expected");
    }catch (Exception e) {
      // expected
    }    
  }
    
  @Test
  public void testStartProcessEngine() throws Exception {
    DummyProcessEngineConfiguration processEngineConfiguration = new DummyProcessEngineConfiguration(true, "default", ENGINE_DS1, "audit", true, false);
    
    Assert.assertEquals(0, processEngineService.getProcessEngines().size());
    Assert.assertEquals(0, processEngineService.getProcessEngineNames().size());
    
    Future<ProcessEngineStartOperation> startProcessEngine = processEngineService.startProcessEngine(processEngineConfiguration);
    
    ProcessEngineStartOperation processEngineStartOperation = startProcessEngine.get();    
    ProcessEngine processEngine = processEngineStartOperation.getProcessenEngine();
    
    Assert.assertEquals(1, processEngineService.getProcessEngines().size());
    Assert.assertEquals(1, processEngineService.getProcessEngineNames().size());
    
    Assert.assertNotNull(processEngine);
  }
    
  @Test
  public void testStartStopProcessEngine() throws Exception {
    DummyProcessEngineConfiguration processEngineConfiguration = new DummyProcessEngineConfiguration(true, "default", ENGINE_DS1, "audit", true, false);
    
    Assert.assertEquals(0, processEngineService.getProcessEngines().size());
    Assert.assertEquals(0, processEngineService.getProcessEngineNames().size());
    
    Future<ProcessEngineStartOperation> startProcessEngine = processEngineService.startProcessEngine(processEngineConfiguration);
    
    ProcessEngineStartOperation processEngineStartOperation = startProcessEngine.get();    
    Assert.assertTrue(processEngineStartOperation.wasSuccessful());
    ProcessEngine processEngine = processEngineStartOperation.getProcessenEngine();
    
    Assert.assertEquals(1, processEngineService.getProcessEngines().size());
    Assert.assertEquals(1, processEngineService.getProcessEngineNames().size());    
    Assert.assertNotNull(processEngine);
    Assert.assertFalse(null == ProcessEngines.getProcessEngine(processEngine.getName()));   
    
    processEngineService.stopProcessEngine(processEngine);  
    
    Assert.assertTrue(null == ProcessEngines.getProcessEngine(processEngine.getName()));
    Assert.assertEquals(0, processEngineService.getProcessEngines().size());
    Assert.assertEquals(0, processEngineService.getProcessEngineNames().size());
  }
  
  @Test
  public void testStopUnexistingProcessEngineFails() throws Exception {
    try {
      processEngineService.stopProcessEngine("unexistingProcessEngine");
      Assert.fail("exception expected");
    }catch (FoxPlatformException e) {
      // expected
    }
  }
  
  @Test
  public void testStopNullNamedProcessEngineFails() throws Exception {
    try {
      String name = null;
      processEngineService.stopProcessEngine(name);
    Assert.fail("exception expected");
    }catch (FoxPlatformException e) {
      // expected
    }
  }
  
  @Test
  public void testStopNullProcessEngineFails() {
    try {
      ProcessEngine pe = null;
      processEngineService.stopProcessEngine(pe);
      Assert.fail();
    }catch (FoxPlatformException e) {
      // expected
    }
  }
  
  @Test
  public void testStartMultipleEnginesSequentially() throws Exception {
    DummyProcessEngineConfiguration processEngineConfiguration1 = new DummyProcessEngineConfiguration(true, "default1", ENGINE_DS1, "audit", true, false);
    DummyProcessEngineConfiguration processEngineConfiguration2 = new DummyProcessEngineConfiguration(false, "default2", ENGINE_DS2, "audit", true, false);
    
    Assert.assertEquals(0, processEngineService.getProcessEngines().size());
    Assert.assertEquals(0, processEngineService.getProcessEngineNames().size());
    
    Future<ProcessEngineStartOperation> startProcessEngine1 = processEngineService.startProcessEngine(processEngineConfiguration1);
    
    ProcessEngineStartOperation processEngineStartOperation1 = startProcessEngine1.get();    
    Assert.assertTrue(processEngineStartOperation1.wasSuccessful());
    ProcessEngine processEngine1 = processEngineStartOperation1.getProcessenEngine();
    
    Assert.assertEquals(1, processEngineService.getProcessEngines().size());
    Assert.assertEquals(1, processEngineService.getProcessEngineNames().size());
    Assert.assertNotNull(processEngine1);
    Assert.assertFalse(null == ProcessEngines.getProcessEngine(processEngine1.getName()));   
    
    Future<ProcessEngineStartOperation> startProcessEngine2 = processEngineService.startProcessEngine(processEngineConfiguration2);
    
    ProcessEngineStartOperation processEngineStartOperation2 = startProcessEngine2.get();    
    Assert.assertTrue(processEngineStartOperation2.wasSuccessful());
    ProcessEngine processEngine2 = processEngineStartOperation2.getProcessenEngine();
    
    Assert.assertEquals(2, processEngineService.getProcessEngines().size());
    Assert.assertEquals(2, processEngineService.getProcessEngineNames().size());
    Assert.assertNotNull(processEngine2);
    Assert.assertFalse(null == ProcessEngines.getProcessEngine(processEngine2.getName()));      
  }
  
  @Test
  public void testStartMultipleEnginesConcurrently() throws Exception {
    DummyProcessEngineConfiguration processEngineConfiguration1 = new DummyProcessEngineConfiguration(true, "default1", ENGINE_DS1, "audit", true, false);
    DummyProcessEngineConfiguration processEngineConfiguration2 = new DummyProcessEngineConfiguration(false, "default2", ENGINE_DS2, "audit", true, false);
    
    Future<ProcessEngineStartOperation> startProcessEngine1 = processEngineService.startProcessEngine(processEngineConfiguration1);
    Future<ProcessEngineStartOperation> startProcessEngine2 = processEngineService.startProcessEngine(processEngineConfiguration2);
    
    ProcessEngineStartOperation processEngineStartOperation1 = startProcessEngine1.get();    
    Assert.assertTrue(processEngineStartOperation1.wasSuccessful());
    ProcessEngine processEngine1 = processEngineStartOperation1.getProcessenEngine();
    
    Assert.assertNotNull(processEngine1);
    Assert.assertFalse(null == ProcessEngines.getProcessEngine(processEngine1.getName()));   
       
    ProcessEngineStartOperation processEngineStartOperation2 = startProcessEngine2.get();    
    Assert.assertTrue(processEngineStartOperation2.wasSuccessful());
    ProcessEngine processEngine2 = processEngineStartOperation2.getProcessenEngine();
    
    Assert.assertNotNull(processEngine2);
    Assert.assertFalse(null == ProcessEngines.getProcessEngine(processEngine2.getName()));      
    
    Assert.assertEquals(2, processEngineService.getProcessEngines().size());
    Assert.assertEquals(2, processEngineService.getProcessEngineNames().size());
  }
  
  @Test
  public void testStartMultipleEnginesWithSameNameFails() throws Exception {
    DummyProcessEngineConfiguration processEngineConfiguration1 = new DummyProcessEngineConfiguration(true, "default", ENGINE_DS1, "audit", true, false);
    DummyProcessEngineConfiguration processEngineConfiguration2 = new DummyProcessEngineConfiguration(false, "default", ENGINE_DS2, "audit", true, false);
    
    Assert.assertEquals(0, processEngineService.getProcessEngines().size());
    Assert.assertEquals(0, processEngineService.getProcessEngineNames().size());
    
    Future<ProcessEngineStartOperation> startProcessEngine1 = processEngineService.startProcessEngine(processEngineConfiguration1);
    try {
      processEngineService.startProcessEngine(processEngineConfiguration2);
      Assert.fail("exception expected");
    }catch (Exception e) {
      // expected
    }
    
    ProcessEngineStartOperation processEngineStartOperation1 = startProcessEngine1.get();   
    
    Assert.assertTrue(processEngineStartOperation1.wasSuccessful());
    Assert.assertEquals(1, processEngineService.getProcessEngines().size());
    Assert.assertEquals(1, processEngineService.getProcessEngineNames().size());
  }
  
  @Test
  public void testStartMultipleDefaultEnginesFails() throws Exception {
    DummyProcessEngineConfiguration processEngineConfiguration1 = new DummyProcessEngineConfiguration(true, "default1", ENGINE_DS1, "audit", true, false);
    DummyProcessEngineConfiguration processEngineConfiguration2 = new DummyProcessEngineConfiguration(true, "default2", ENGINE_DS2, "audit", true, false);
    
    Future<ProcessEngineStartOperation> startProcessEngine1 = processEngineService.startProcessEngine(processEngineConfiguration1);
    try {
      processEngineService.startProcessEngine(processEngineConfiguration2);
      Assert.fail("exception expected");
    }catch (Exception e) {
      // expected
    }
    
    ProcessEngineStartOperation processEngineStartOperation1 = startProcessEngine1.get();        
    Assert.assertTrue(processEngineStartOperation1.wasSuccessful());
  }
  
  @Test
  public void testStartStopStartDefaultEngine() throws Exception {
    DummyProcessEngineConfiguration processEngineConfiguration = new DummyProcessEngineConfiguration(true, "default", ENGINE_DS1, "audit", true, false);
    
    Future<ProcessEngineStartOperation> startProcessEngine = processEngineService.startProcessEngine(processEngineConfiguration);
    
    ProcessEngineStartOperation processEngineStartOperation = startProcessEngine.get();    
    Assert.assertTrue(processEngineStartOperation.wasSuccessful());
    ProcessEngine processEngine = processEngineStartOperation.getProcessenEngine();
    
    Assert.assertNotNull(processEngine);
    Assert.assertFalse(null == ProcessEngines.getProcessEngine(processEngine.getName()));   
    
    Assert.assertEquals(processEngineService.getDefaultProcessEngine(), processEngine);
    
    processEngineService.stopProcessEngine(processEngine);
      
    Assert.assertTrue(null == ProcessEngines.getProcessEngine(processEngine.getName()));
    
    try{
      processEngineService.getDefaultProcessEngine();
      Assert.fail("expected exception");
    }catch (FoxPlatformException e) {
      // expected
    }
    
    startProcessEngine = processEngineService.startProcessEngine(processEngineConfiguration);
    
    processEngineStartOperation = startProcessEngine.get();    
    Assert.assertTrue(processEngineStartOperation.wasSuccessful());
    processEngine = processEngineStartOperation.getProcessenEngine();
    
    Assert.assertNotNull(processEngine);
    Assert.assertFalse(null == ProcessEngines.getProcessEngine(processEngine.getName()));
    
    Assert.assertEquals(processEngineService.getDefaultProcessEngine(), processEngine);
  }

}
