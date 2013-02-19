package org.camunda.bpm.application.impl.deployment.metadata;

import java.util.List;

import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;

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
