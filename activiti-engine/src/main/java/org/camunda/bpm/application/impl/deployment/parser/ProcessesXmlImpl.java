package org.camunda.bpm.application.impl.deployment.parser;

import java.util.List;

import org.camunda.bpm.application.impl.deployment.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.deployment.spi.ProcessEngineXml;
import org.camunda.bpm.application.impl.deployment.spi.ProcessesXml;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessesXmlImpl implements ProcessesXml {
  
  private List<ProcessEngineXml> processEngineXmls;
  private List<ProcessArchiveXml> processArchiveXmls;
  
  public ProcessesXmlImpl(List<ProcessEngineXml> processEngineXmls, List<ProcessArchiveXml> processArchiveXmls) {
    this.processEngineXmls = processEngineXmls;
    this.processArchiveXmls = processArchiveXmls;
  }

  public List<ProcessEngineXml> getProcessEngines() {
    return processEngineXmls;
  }

  public List<ProcessArchiveXml> getProcessArchives() {
    return processArchiveXmls;
  }

}
