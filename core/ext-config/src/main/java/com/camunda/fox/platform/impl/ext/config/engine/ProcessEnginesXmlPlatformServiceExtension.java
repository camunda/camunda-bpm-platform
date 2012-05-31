package com.camunda.fox.platform.impl.ext.config.engine;

import com.camunda.fox.platform.impl.ext.config.engine.spi.ProcessEnginesXmlSupport;
import com.camunda.fox.platform.impl.ext.util.ServiceLoaderUtil;
import com.camunda.fox.platform.impl.service.PlatformService;
import com.camunda.fox.platform.impl.service.PlatformServiceExtensionAdapter;

/**
 * Adds support for loading the META-INF/process-engines.xml file when starting
 * and stopping the platform services
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessEnginesXmlPlatformServiceExtension extends PlatformServiceExtensionAdapter {

  protected ProcessEnginesXmlSupport processEnginesXmlSupport;
  
  public int getPrecedence() {
    return 200;
  }

  public void onPlatformServiceStart(PlatformService platformService) {
    processEnginesXmlSupport = ServiceLoaderUtil.loadService(ProcessEnginesXmlSupport.class, ProcessEnginesXmlSupportImpl.class);
    processEnginesXmlSupport.startProcessEngines(platformService);
  }

  public void onPlatformServiceStop(PlatformService platformService) {
    processEnginesXmlSupport.stopProcessEngines(platformService);
  }

}
