package com.camunda.fox.platform.jobexecutor.impl.acquisition;

import org.activiti.engine.impl.jobexecutor.AcquireJobsRunnable;

import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionStrategy;

/**
 * <p>The default {@link JobAcquisitionStrategy#SEQENTIAL} job acquisition strategy.</p>
 * 
 * <p>You can override this strategy using the {@link java.util.ServiceLoader} mechanism, 
 * providing a custom implementation of the {@link JobAcquisitionStrategy} interface such 
 * that {@link JobAcquisitionStrategy#getJobAcquisitionName()} returns 
 * {@link JobAcquisitionStrategy#SEQENTIAL}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class SequentialJobAcquisition implements JobAcquisitionStrategy {

  public String getJobAcquisitionName() {
    return SEQENTIAL;
  }

  public AcquireJobsRunnable getAcquireJobsRunnable(JobAcquisition jobAcquisition) {
    return new SequentialJobAcquisitionRunnable(jobAcquisition);
  }
}
