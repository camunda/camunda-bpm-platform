package com.camunda.fox.platform.impl.test;

import java.util.HashMap;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.activiti.engine.ProcessEngine;
import org.junit.Test;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService.ProcessArchiveInstallation;
import com.camunda.fox.platform.api.ProcessEngineService.ProcessEngineStartOperation;
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
    ProcessArchiveInstallation installProcessArchive = processArchiveService.installProcessArchive(processArchive);    
    
    Assert.assertNotNull(installProcessArchive.getProcessEngine());
    Assert.assertNull(installProcessArchive.getProcessEngineDeploymentId());
    
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
    
    processArchiveService.installProcessArchive(processArchive);    
    
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
    
    processArchiveService.unInstallProcessArchive(processArchive);
    
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

    processArchiveService.installProcessArchive(processArchive);
    
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
    
    processArchiveService.unInstallProcessArchive(processArchive);
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());    
  }
  
  @Test
  public void testInstallMultiple() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives(engine1.getName()).size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    processArchiveService.installProcessArchive(processArchive);
  
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1).size());
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives(engine1.getName()).size()); 
    
    ProcessArchive processArchive2 = new DummyProcessArchive("archive2", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    processArchiveService.installProcessArchive(processArchive2);
    
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
    processArchiveService.installProcessArchive(processArchive);
   
    ProcessArchive processArchive2 = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    try {
      processArchiveService.installProcessArchive(processArchive2);
      Assert.fail("expected");
    }catch (FoxPlatformException e) {
      // expected
    }
        
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
    processArchiveService.installProcessArchive(processArchive);
    
    ProcessArchive processArchive2 = new DummyProcessArchive("archive1", engine2.getName(), false, new HashMap<String, byte[]>(), true);    
    try {
      processArchiveService.installProcessArchive(processArchive2);
      Assert.fail("expected");
    }catch (FoxPlatformException e) {
      // expected
    }
    
    Assert.assertEquals(1, processArchiveService.getInstalledProcessArchives().size());
  }
  
  @Test
  public void testInstallToUnexistingProcessEngineFails() throws Exception {    
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", "unexistingProcessEngine", false, new HashMap<String, byte[]>(), true);      
    try {
      processArchiveService.installProcessArchive(processArchive);
      Assert.fail("expected");
    }catch (FoxPlatformException e) {
      // expected
    }
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
  }
  
  @Test
  public void testInstallToStoppedEngineFails() throws Exception {    
    ProcessEngine engine1 = startProcessEgine1();    
    processEngineService.stopProcessEngine(engine1);
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, new HashMap<String, byte[]>(), true);    
    try {
      processArchiveService.installProcessArchive(processArchive);
      Assert.fail("expected");
    }catch (FoxPlatformException e) {
      // expected
    }
    
    Assert.assertEquals(0, processArchiveService.getInstalledProcessArchives().size());
  }
    
}

