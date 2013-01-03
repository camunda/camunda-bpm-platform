package com.camunda.fox.platform.impl.service;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Test;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.api.ProcessArchiveService.ProcessArchiveInstallation;
import com.camunda.fox.platform.api.ProcessEngineService.ProcessEngineStartOperation;
import com.camunda.fox.platform.impl.service.util.DummyProcessArchive;
import com.camunda.fox.platform.impl.service.util.DummyProcessEngineConfiguration;
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
  
  @Test
  public void testGetProcessArchiveByProcessDefinitionKey()  throws Exception {
    ProcessEngine engine1 = startProcessEgine1();
    
    HashMap<String, byte[]> processResources = new HashMap<String, byte[]>();
    String processResource = IoUtil.readFileAsString("com/camunda/fox/platform/impl/service/testDeployProcessArchive.bpmn20.xml");
    processResources.put("testDeployProcessArchive.bpmn20.xml", processResource.getBytes());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, processResources, true);    
    processArchiveService.installProcessArchive(processArchive);
    
    ProcessArchive retreivedArchive = processArchiveService.getProcessArchiveByProcessDefinitionKey("testDeployProcessArchive", engine1.getName());
    Assert.assertNotNull(retreivedArchive);
    Assert.assertEquals(processArchive, retreivedArchive);
  }
  
  @Test
  public void testGetProcessArchiveByProcessDefinitionKeyUnknownKeyFails()  throws Exception {
    ProcessEngine engine1 = startProcessEgine1();
    
    HashMap<String, byte[]> processResources = new HashMap<String, byte[]>();
    String processResource = IoUtil.readFileAsString("com/camunda/fox/platform/impl/service/testDeployProcessArchive.bpmn20.xml");
    processResources.put("testDeployProcessArchive.bpmn20.xml", processResource.getBytes());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, processResources, true);    
    processArchiveService.installProcessArchive(processArchive);
    
    try {
      processArchiveService.getProcessArchiveByProcessDefinitionKey("unknownKey", engine1.getName());
      Assert.fail("exception expected.");
    }catch (FoxPlatformException e) {
      // expected
    }
  }
  
  @Test
  public void testGetProcessArchiveByProcessDefinitionKeyUnknownEngineFails()  throws Exception {
    ProcessEngine engine1 = startProcessEgine1();
    
    HashMap<String, byte[]> processResources = new HashMap<String, byte[]>();
    String processResource = IoUtil.readFileAsString("com/camunda/fox/platform/impl/service/testDeployProcessArchive.bpmn20.xml");
    processResources.put("testDeployProcessArchive.bpmn20.xml", processResource.getBytes());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, processResources, true);    
    processArchiveService.installProcessArchive(processArchive);
    
    try {
      processArchiveService.getProcessArchiveByProcessDefinitionKey("testDeployProcessArchive", "unknownEngine");
      Assert.fail("exception expected.");
    }catch (FoxPlatformException e) {
      // expected
    }
  }
  
  @Test
  public void testGetProcessArchiveByProcessDefinitionId()  throws Exception {
    ProcessEngine engine1 = startProcessEgine1();
    
    HashMap<String, byte[]> processResources = new HashMap<String, byte[]>();
    String processResource = IoUtil.readFileAsString("com/camunda/fox/platform/impl/service/testDeployProcessArchive.bpmn20.xml");
    processResources.put("testDeployProcessArchive.bpmn20.xml", processResource.getBytes());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, processResources, true);    
    processArchiveService.installProcessArchive(processArchive);
    
    ProcessDefinition processDefinition = engine1.getRepositoryService().createProcessDefinitionQuery().singleResult();
    
    ProcessArchive retreivedArchive = processArchiveService.getProcessArchiveByProcessDefinitionId(processDefinition.getId(), engine1.getName());
    Assert.assertNotNull(retreivedArchive);
    Assert.assertEquals(processArchive, retreivedArchive);
  }
  
  @Test
  public void testGetProcessArchiveByProcessDefinitionIdUnknownDefinition()  throws Exception {
    ProcessEngine engine1 = startProcessEgine1();
    
    HashMap<String, byte[]> processResources = new HashMap<String, byte[]>();
    String processResource = IoUtil.readFileAsString("com/camunda/fox/platform/impl/service/testDeployProcessArchive.bpmn20.xml");
    processResources.put("testDeployProcessArchive.bpmn20.xml", processResource.getBytes());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, processResources, true);    
    processArchiveService.installProcessArchive(processArchive);

    try { 
      processArchiveService.getProcessArchiveByProcessDefinitionId("bogus", engine1.getName());
      Assert.fail("Exception expected");
    }catch (FoxPlatformException e) {
      // expected
    }
  }
  
  @Test
  public void testGetProcessArchiveByProcessDefinitionIdUnknownEngine()  throws Exception {
    ProcessEngine engine1 = startProcessEgine1();
    
    HashMap<String, byte[]> processResources = new HashMap<String, byte[]>();
    String processResource = IoUtil.readFileAsString("com/camunda/fox/platform/impl/service/testDeployProcessArchive.bpmn20.xml");
    processResources.put("testDeployProcessArchive.bpmn20.xml", processResource.getBytes());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, processResources, true);    
    processArchiveService.installProcessArchive(processArchive);
    
    ProcessDefinition processDefinition = engine1.getRepositoryService().createProcessDefinitionQuery().singleResult();

    try { 
      processArchiveService.getProcessArchiveByProcessDefinitionId(processDefinition.getId(), "bogus");
      Assert.fail("Exception expected");
    }catch (FoxPlatformException e) {
      // expected
    }
  }
  
  @Test
  public void testGetProcessArchiveByProcessDefinitionIdAfterUpdate()  throws Exception {
    ProcessEngine engine1 = startProcessEgine1();
    
    HashMap<String, byte[]> processResources = new HashMap<String, byte[]>();
    String processResource = IoUtil.readFileAsString("com/camunda/fox/platform/impl/service/testDeployProcessArchive.bpmn20.xml");
    processResources.put("testDeployProcessArchive.bpmn20.xml", processResource.getBytes());
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, processResources, false);    
    processArchiveService.installProcessArchive(processArchive);
    
    ProcessDefinition processDefinitionBeforeUpdate = engine1.getRepositoryService().createProcessDefinitionQuery().singleResult();
    
    processArchiveService.unInstallProcessArchive(processArchive);
    
    processResources = new HashMap<String, byte[]>();
    processResource = IoUtil.readFileAsString("com/camunda/fox/platform/impl/service/testDeployProcessArchiveUpdate.bpmn20.xml");
    processResources.put("testDeployProcessArchive.bpmn20.xml", processResource.getBytes());
    
    processArchive = new DummyProcessArchive("archive1", engine1.getName(), false, processResources, true);    
    processArchiveService.installProcessArchive(processArchive);
    
    List<ProcessDefinition> processDefinitionsAfterUpdate = engine1.getRepositoryService().createProcessDefinitionQuery().list();
    Assert.assertEquals(2, processDefinitionsAfterUpdate.size());
    
    ProcessDefinition processDefinitionAfterUpdate = null;
    for (ProcessDefinition processDefinition : processDefinitionsAfterUpdate) {
      if(!processDefinition.getId().equals(processDefinitionBeforeUpdate.getId())) {
        processDefinitionAfterUpdate = processDefinition;
      }
    }   
    
    ProcessArchive retreivedArchive = processArchiveService.getProcessArchiveByProcessDefinitionId(processDefinitionBeforeUpdate.getId(), engine1.getName());
    Assert.assertNotNull(retreivedArchive);
    Assert.assertEquals(processArchive, retreivedArchive);
    
    retreivedArchive = processArchiveService.getProcessArchiveByProcessDefinitionId(processDefinitionAfterUpdate.getId(), engine1.getName());
    Assert.assertNotNull(retreivedArchive);
    Assert.assertEquals(processArchive, retreivedArchive);
  }
    
}

