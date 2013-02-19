package org.camunda.bpm.application.impl.deployment.spi;

import java.util.List;

/**
 * <p>Java API representation of the bpm-platform.xml file.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public interface BpmPlatformXml {
  
  /**
   * @return the {@link JobExecutorXml} configuration of the JobExecutor. 
   */
  public JobExecutorXml getJobExecutor();
  
  /**
   * @return A {@link List} of {@link ProcessEngineXml} Metadata Items representing process engine configurations. 
   */
  public List<ProcessEngineXml> getProcessEngines();
  
}
