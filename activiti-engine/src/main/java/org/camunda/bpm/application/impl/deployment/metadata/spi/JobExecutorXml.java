package org.camunda.bpm.application.impl.deployment.metadata.spi;

import java.util.List;

/**
 * <p>Deployment Metadata for the JobExecutor Service.</p>
 * 
 *  
 * @author Daniel Meyer
 *
 */
public interface JobExecutorXml {
  
  /**
   * @return a list of configured JobAcquisitions.
   */
  public List<JobAcquisitionXml> getJobAcquisitions();

}
