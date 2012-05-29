package com.camunda.fox.platform.impl.ext.config.engine.spi;

import java.util.List;

import com.camunda.fox.platform.impl.ext.config.engine.ProcessEnginesXml;


/**
 * 
 * @author Daniel Meyer
 * 
 */
public interface ProcessEnginesXmlParser {

  public List<ProcessEnginesXml> parseProcessEnginesXml(String processEnginesXmlLocation);

}