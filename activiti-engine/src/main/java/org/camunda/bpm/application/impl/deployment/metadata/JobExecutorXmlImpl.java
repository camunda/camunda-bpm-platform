package org.camunda.bpm.application.impl.deployment.metadata;

import java.util.List;

import org.camunda.bpm.application.impl.deployment.metadata.spi.JobAcquisitionXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.JobExecutorXml;

/**
 * <p>Implementation of the {@link JobExecutorXml}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class JobExecutorXmlImpl implements JobExecutorXml {
  
  protected List<JobAcquisitionXml> jobAcquisitions;

  public List<JobAcquisitionXml> getJobAcquisitions() {
    return jobAcquisitions;
  }
  
  public void setJobAcquisitions(List<JobAcquisitionXml> jobAcquisitions) {
    this.jobAcquisitions = jobAcquisitions;
  }

}
