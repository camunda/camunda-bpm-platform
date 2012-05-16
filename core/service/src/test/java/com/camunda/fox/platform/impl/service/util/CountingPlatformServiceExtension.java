package com.camunda.fox.platform.impl.service.util;

import com.camunda.fox.platform.impl.service.PlatformService;
import com.camunda.fox.platform.impl.service.ProcessEngineController;
import com.camunda.fox.platform.impl.service.spi.PlatformServiceExtension;
import com.camunda.fox.platform.spi.ProcessArchive;


public class CountingPlatformServiceExtension implements PlatformServiceExtension {
  
  public int onPlatformServiceStart=0;
  public int onPlatformServiceStop=0;
  public int beforeProcessEngineControllerStart=0;
  public int afterProcessEngineControllerStart=0;
  public int beforeProcessEngineControllerStop=0;
  public int afterProcessEngineControllerStop=0;
  public int beforeProcessArchiveInstalled=0;
  public int afterProcessArchiveInstalled=0;
  public int beforeProcessArchiveUninstalled=0;
  public int afterProcessArchiveUninstalled=0;
  
  public static CountingPlatformServiceExtension instance;
  
  public CountingPlatformServiceExtension() {
    instance = this;
  }
  
  public void onPlatformServiceStart(PlatformService platformService) {
    onPlatformServiceStart++;
  }

  public void onPlatformServiceStop(PlatformService platformService) {
    onPlatformServiceStop++;
  }

  public void beforeProcessEngineControllerStart(ProcessEngineController processEngineController) {
    beforeProcessEngineControllerStart++;
  }

  public void afterProcessEngineControllerStart(ProcessEngineController processEngineController) {
    afterProcessEngineControllerStart++;
  }

  public void beforeProcessEngineControllerStop(ProcessEngineController processEngineController) {
    beforeProcessEngineControllerStop++;
  }

  public void afterProcessEngineControllerStop(ProcessEngineController processEngineController) {
    afterProcessEngineControllerStop++;
  }

  public void beforeProcessArchiveInstalled(ProcessArchive processArchive, ProcessEngineController processEngineController) {
    beforeProcessArchiveInstalled++;
  }

  public void afterProcessArchiveInstalled(ProcessArchive processArchive, ProcessEngineController processEngineController, String deploymentId) {
    afterProcessArchiveInstalled++;
  }

  public void beforeProcessArchiveUninstalled(ProcessArchive processArchive, ProcessEngineController processEngineController) {
    beforeProcessArchiveUninstalled++;
  }

  public void afterProcessArchiveUninstalled(ProcessArchive processArchive, ProcessEngineController processEngineController) {
    afterProcessArchiveUninstalled++;
  }

}
