package com.camunda.fox.platform.impl.ext.config.engine;

import org.junit.Before;

import com.camunda.fox.platform.impl.ext.config.engine.spi.ProcessEnginesXmlSupport;

/**
 * 
 * @author Daniel Meyer
 * 
 */
public class TestProcessEnginesXmlSupport {

  private ProcessEnginesXmlSupport processEnginesXmlSupport;

  @Before
  public void init() {
    processEnginesXmlSupport = new ProcessEnginesXmlSupportImpl();
  }
    
}
