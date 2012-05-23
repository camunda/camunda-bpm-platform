package com.camunda.fox.platform.impl.ext.config.engine;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * 
 * @author Daniel Meyer
 */
@XmlRootElement(name = "process-engines")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessEnginesXml {
  
  @XmlTransient
  public String resourceName;

  @XmlElements(@XmlElement(name = "process-engine", type = ProcessEngineXml.class))
  public List<ProcessEngineConfiguration> processEngines = new ArrayList();
}