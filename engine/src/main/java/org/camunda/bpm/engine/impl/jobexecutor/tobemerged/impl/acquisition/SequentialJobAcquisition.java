package org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.acquisition;

import org.camunda.bpm.engine.impl.jobexecutor.AcquireJobsRunnable;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.spi.JobAcquisitionStrategy;


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
    return SEQUENTIAL;
  }

  public AcquireJobsRunnable getAcquireJobsRunnable(JobExecutor jobAcquisition) {
    return new SequentialJobAcquisitionRunnable(jobAcquisition);
  }
}
