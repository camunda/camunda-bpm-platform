package com.camunda.fox.platform.impl.ext.config.engine;

import org.camunda.bpm.application.spi.ProcessApplication;

import com.camunda.fox.client.impl.ProcessArchiveExtensionAdapter;
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

  public void beforeProcessArchiveStart(ProcessApplication processArchiveSupport) {
    processEnginesXmlSupport = ServiceLoaderUtil.loadService(ProcessEnginesXmlSupport.class, ProcessEnginesXmlSupportImpl.class);
    processEnginesXmlSupport.startProcessEngines(processArchiveSupport.getProcessEngineService());
  }

  public void afterProcessArchiveStop(ProcessApplication processArchiveSupport) {
    processEnginesXmlSupport.stopProcessEngines(processArchiveSupport.getProcessEngineService());
  }
}
