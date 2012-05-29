package com.camunda.fox.platform.impl.service;

import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;

import com.camunda.fox.platform.impl.service.util.CountingPlatformServiceExtension;
import com.camunda.fox.platform.impl.service.util.DummyProcessArchive;
import com.camunda.fox.platform.impl.service.util.DummyProcessEngineConfiguration;
import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * 
 * @author Daniel Meyer
 *
 */
public class TestPlatformServiceExtensions extends PlatformServiceTest {
  
  @Test
  public void testPlatformServiceLifecycleExtensions() {
    CountingPlatformServiceExtension.instance = null;
    
    platformService.start();    
    CountingPlatformServiceExtension extensionInstance = CountingPlatformServiceExtension.instance;    
    Assert.assertNotNull(extensionInstance);
    Assert.assertEquals(1, extensionInstance.onPlatformServiceStart);
    Assert.assertEquals(0, extensionInstance.onPlatformServiceStop);  
       
    platformService.stop();
    Assert.assertEquals(extensionInstance, CountingPlatformServiceExtension.instance);
    Assert.assertEquals(1, extensionInstance.onPlatformServiceStart);
    Assert.assertEquals(1, extensionInstance.onPlatformServiceStop);  
  }
  
  @Test
  public void testProcessEngineControllerLifecycleExtensions() throws Exception{
    CountingPlatformServiceExtension.instance = null;
    platformService.start();
    
    CountingPlatformServiceExtension extensionInstance = CountingPlatformServiceExtension.instance;    
    Assert.assertNotNull(extensionInstance);   
    Assert.assertEquals(0, extensionInstance.beforeProcessEngineControllerStart);
    Assert.assertEquals(0, extensionInstance.afterProcessEngineControllerStart);
    Assert.assertEquals(0, extensionInstance.beforeProcessEngineControllerStop);
    Assert.assertEquals(0, extensionInstance.afterProcessEngineControllerStop);
    
    DummyProcessEngineConfiguration processEngineConfiguration = new DummyProcessEngineConfiguration(true, "default", ENGINE_DS1, "audit", true, false);    
    processEngineService.startProcessEngine(processEngineConfiguration).get();
    
    Assert.assertEquals(extensionInstance, CountingPlatformServiceExtension.instance);
    Assert.assertEquals(1, extensionInstance.beforeProcessEngineControllerStart);
    Assert.assertEquals(1, extensionInstance.afterProcessEngineControllerStart);
    Assert.assertEquals(0, extensionInstance.beforeProcessEngineControllerStop);
    Assert.assertEquals(0, extensionInstance.afterProcessEngineControllerStop);
            
    processEngineService.stopProcessEngine("default");  
    
    Assert.assertEquals(extensionInstance, CountingPlatformServiceExtension.instance); 
    Assert.assertEquals(1, extensionInstance.beforeProcessEngineControllerStart);
    Assert.assertEquals(1, extensionInstance.afterProcessEngineControllerStart);
    Assert.assertEquals(1, extensionInstance.beforeProcessEngineControllerStop);
    Assert.assertEquals(1, extensionInstance.afterProcessEngineControllerStop);
    
    platformService.stop();
  }
  
  @Test
  public void testProcessArchiveInstallationExtensions() throws Exception {
    CountingPlatformServiceExtension.instance = null;
    platformService.start();
    
    CountingPlatformServiceExtension extensionInstance = CountingPlatformServiceExtension.instance;    
    Assert.assertNotNull(extensionInstance);
    Assert.assertEquals(0, extensionInstance.beforeProcessArchiveInstalled);
    Assert.assertEquals(0, extensionInstance.afterProcessArchiveInstalled);
    Assert.assertEquals(0, extensionInstance.beforeProcessArchiveUninstalled);
    Assert.assertEquals(0, extensionInstance.afterProcessArchiveUninstalled);
     
    DummyProcessEngineConfiguration processEngineConfiguration = new DummyProcessEngineConfiguration(true, "default", ENGINE_DS1, "audit", true, false);    
    processEngineService.startProcessEngine(processEngineConfiguration).get();
    
    ProcessArchive processArchive = new DummyProcessArchive("archive1", null, false, new HashMap<String, byte[]>(), true);     
    processArchiveService.installProcessArchive(processArchive);    
    
    Assert.assertEquals(extensionInstance, CountingPlatformServiceExtension.instance);
    Assert.assertEquals(1, extensionInstance.beforeProcessArchiveInstalled);
    Assert.assertEquals(1, extensionInstance.afterProcessArchiveInstalled);
    Assert.assertEquals(0, extensionInstance.beforeProcessArchiveUninstalled);
    Assert.assertEquals(0, extensionInstance.afterProcessArchiveUninstalled);
    
    processArchiveService.unInstallProcessArchive(processArchive);
       
    Assert.assertEquals(extensionInstance, CountingPlatformServiceExtension.instance);  
    Assert.assertEquals(1, extensionInstance.beforeProcessArchiveInstalled);
    Assert.assertEquals(1, extensionInstance.afterProcessArchiveInstalled);
    Assert.assertEquals(1, extensionInstance.beforeProcessArchiveUninstalled);
    Assert.assertEquals(1, extensionInstance.afterProcessArchiveUninstalled);
    
    platformService.stop();
  }

}
