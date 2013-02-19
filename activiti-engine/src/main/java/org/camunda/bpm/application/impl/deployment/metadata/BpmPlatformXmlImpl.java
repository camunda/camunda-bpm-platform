package org.camunda.bpm.application.impl.deployment.metadata;

import java.util.List;

import org.camunda.bpm.application.impl.deployment.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.JobExecutorXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;

/**
 * <p>Implementation of the BpmPlatformXml SPI</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class BpmPlatformXmlImpl implements BpmPlatformXml {

  protected JobExecutorXml jobExecutor;

  protected List<ProcessEngineXml> processEngines;
  
  public BpmPlatformXmlImpl(JobExecutorXml jobExecutor, List<ProcessEngineXml> processEngines) {
    this.jobExecutor = jobExecutor;
    this.processEngines = processEngines;
  }

  public List<ProcessEngineXml> getProcessEngines() {
    return processEngines;
  }

  public void setProcessEngines(List<ProcessEngineXml> processEngines) {
    this.processEngines = processEngines;
  }

  public JobExecutorXml getJobExecutor() {
    return jobExecutor;
  }

  public void setJobExecutor(JobExecutorXml jobExecutor) {
    this.jobExecutor = jobExecutor;
  }

}
