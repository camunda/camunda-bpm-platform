package com.camunda.fox.platform.impl.test.service;

import java.util.HashMap;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.activiti.engine.ProcessEngine;
import org.junit.Test;

import com.camunda.fox.platform.api.ProcessArchiveService.ProcessArchiveInstallOperation;
import com.camunda.fox.platform.api.ProcessArchiveService.ProcessArchiveUninstallOperation;
import com.camunda.fox.platform.api.ProcessEngineService.ProcessEngineStartOperation;
import com.camunda.fox.platform.api.ProcessEngineService.ProcessEngineStopOperation;
import com.camunda.fox.platform.impl.test.PlatformServiceTest;
import com.camunda.fox.platform.impl.test.util.DummyProcessArchive;
import com.camunda.fox.platform.impl.test.util.DummyProcessEngineConfiguration;
import com.camunda.fox.platform.spi.ProcessArchive;


public class TestProcessArchiveService extends PlatformServiceTest {

  protected ProcessEngine startProcessEgine1() throws Exception{
    DummyProcessEngineConfiguration processEngineConfiguration = new DummyProcessEngineConfiguration(true, "engine1", ENGINE_DS1, "audit", true, false);
    
    Future<ProcessEngineStartOperation> startProcessEngine = processEngineService.startProcessEngine(processEngineConfiguration);
    
    ProcessEngineStartOperation processEngineStartOperation = startProcessEngine.get();    
    ProcessEngine processEngine = processEngineStartOperation.getProcessenEngine();
    return processEngine;
  }
  
  protected ProcessEngine startProcessEgine2() throws Exception{
    DummyProcessEngineConfiguration processEngineConfiguration = new DummyProcessEngineConfiguration(false, "engine2", ENGINE_DS2, "audit", true, false);
    
    Future<ProcessEngineStartOperation> startProcessEngine = processEngineService.startProcessEngine(processEngineConfiguration);
    
    ProcessEngineStartOperation processEngineStartOperation = startProcessEngine.get();    
    ProcessEngine processEngine = processEngineStartOperation.getProcessenEngine();    
    return processEngine;    
  }
  
  @Test
  public void testInstallProcessArchive() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();
    
    Assert.assertTrue(processArchiveInstallOperation.wasSuccessful());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
  }
  
  @Test
  public void testInstallUninstallProcessArchiveToDefaultEngine() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", null, false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();
    
    Assert.assertTrue(processArchiveInstallOperation.wasSuccessful());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
    
    Future<ProcessArchiveUninstallOperation> unInstallProcessArchive = processArchiveService.unInstallProcessArchive(processArchive);
    ProcessArchiveUninstallOperation processArchiveUninstallOperation = unInstallProcessArchive.get();
    
    Assert.assertTrue(processArchiveUninstallOperation.wasSuccessful());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());     
  }
  
  @Test
  public void testInstallUninstallProcessArchive() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();
    
    Assert.assertTrue(processArchiveInstallOperation.wasSuccessful());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
    
    Future<ProcessArchiveUninstallOperation> unInstallProcessArchive = processArchiveService.unInstallProcessArchive(processArchive);
    ProcessArchiveUninstallOperation processArchiveUninstallOperation = unInstallProcessArchive.get();
    
    Assert.assertTrue(processArchiveUninstallOperation.wasSuccessful());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
  }
  
  @Test
  public void testInstallMultipleSequentially() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();

    Assert.assertTrue(processArchiveInstallOperation.wasSuccessful());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1.getName()).size()); 
    
    ProcessArchive processArchive2 = new DummyProcessArchive("archive2", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive2 = processArchiveService.installProcessArchive(processArchive2);
    ProcessArchiveInstallOperation processArchiveInstallOperation2 = installProcessArchive2.get();
    
    Assert.assertTrue(processArchiveInstallOperation2.wasSuccessful());
    Assert.assertEquals(2, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(2, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(2, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
  }
  
  @Test
  public void testInstallMultipleConcurrently() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    ProcessArchive processArchive2 = new DummyProcessArchive("archive2", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive2 = processArchiveService.installProcessArchive(processArchive2);
    
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();  
    ProcessArchiveInstallOperation processArchiveInstallOperation2 = installProcessArchive2.get();
    
    Assert.assertTrue(processArchiveInstallOperation2.wasSuccessful() && processArchiveInstallOperation.wasSuccessful());
    Assert.assertEquals(2, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(2, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(2, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
  }
  
  @Test
  public void testInstallMultipleWithSameNameFails() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    ProcessArchive processArchive2 = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive2 = processArchiveService.installProcessArchive(processArchive2);
    
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();  
    ProcessArchiveInstallOperation processArchiveInstallOperation2 = installProcessArchive2.get();
    
    Assert.assertFalse(processArchiveInstallOperation2.wasSuccessful() && processArchiveInstallOperation.wasSuccessful());
    Assert.assertTrue(processArchiveInstallOperation2.wasSuccessful() || processArchiveInstallOperation.wasSuccessful());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
  }
  
  @Test
  public void testInstallMultipleWithSameNameToDifferentEnginesFails() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();
    ProcessEngine engine2 = startProcessEgine2();
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    ProcessArchive processArchive2 = new DummyProcessArchive("archive1", engine2.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive2 = processArchiveService.installProcessArchive(processArchive2);
    
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();  
    ProcessArchiveInstallOperation processArchiveInstallOperation2 = installProcessArchive2.get();
    
    Assert.assertFalse(processArchiveInstallOperation2.wasSuccessful() && processArchiveInstallOperation.wasSuccessful());
    Assert.assertTrue(processArchiveInstallOperation2.wasSuccessful() || processArchiveInstallOperation.wasSuccessful());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
  }
  
  @Test
  public void testInstallToUnexistingProcessEngineFails() throws Exception {    
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", "unexistingProcessEngine", false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();  
    
    Assert.assertFalse(processArchiveInstallOperation.wasSuccessful());
    Assert.assertNotNull(processArchiveInstallOperation.getException());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
  }
  
  @Test
  public void testInstallToStoppedEngineFails() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();    
    processEngineService.stopProcessEngine(engine1).get();
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();  
    
    Assert.assertFalse(processArchiveInstallOperation.wasSuccessful());
    Assert.assertNotNull(processArchiveInstallOperation.getException());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
  }
  
  @Test
  public void testInstallToStoppedEngineConcurrentlyFails() throws Exception {    
    
    ProcessEngine engine1 = startProcessEgine1();   
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
 
    Future<ProcessEngineStopOperation> stopProcessEngine = processEngineService.stopProcessEngine(engine1);       
    Future<ProcessArchiveInstallOperation> installProcessArchive = processArchiveService.installProcessArchive(processArchive);
    
    ProcessArchiveInstallOperation processArchiveInstallOperation = installProcessArchive.get();    
    ProcessEngineStopOperation processEngineStopOperation = stopProcessEngine.get();
            
    Assert.assertFalse(processArchiveInstallOperation.wasSuccessful() && processEngineStopOperation.wasSuccessful());
    Assert.assertTrue(processArchiveInstallOperation.wasSuccessful() || processEngineStopOperation.wasSuccessful());    
  }
  
}

