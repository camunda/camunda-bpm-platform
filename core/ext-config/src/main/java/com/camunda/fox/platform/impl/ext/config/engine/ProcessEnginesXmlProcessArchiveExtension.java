package com.camunda.fox.platform.impl.ext.config.engine;

import com.camunda.fox.client.impl.ProcessArchiveExtensionAdapter;
import com.camunda.fox.client.impl.ProcessArchiveSupport;
import com.camunda.fox.platform.impl.ext.config.engine.spi.ProcessEnginesXmlSupport;
import com.camunda.fox.platform.impl.ext.util.ServiceLoaderUtil;

/**
 * Adds Support for the META-INF/process-engines.xml file in a Process
 * Application.
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessEnginesXmlProcessArchiveExtension extends ProcessArchiveExtensionAdapter {

  protected ProcessEnginesXmlSupport processEnginesXmlSupport;

  public void beforeProcessArchiveStart(ProcessArchiveSupport processArchiveSupport) {
    processEnginesXmlSupport = ServiceLoaderUtil.loadService(ProcessEnginesXmlSupport.class, ProcessEnginesXmlSupportImpl.class);
    processEnginesXmlSupport.startProcessEngines(processArchiveSupport.getProcessEngineService());
  }

  public void afterProcessArchiveStop(ProcessArchiveSupport processArchiveSupport) {
    processEnginesXmlSupport.stopProcessEngines(processArchiveSupport.getProcessEngineService());
  }
}
