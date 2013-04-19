package org.camunda.bpm.engine.impl.jobexecutor.tobemerged.spi;

import org.camunda.bpm.engine.impl.jobexecutor.AcquireJobsRunnable;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;

/**
 * <p>A job acquisition strategy implements the 'active' part of a 
 * job Acquisition. A strategy is characterized by a (unique) name 
 * and a custom {@link AcquireJobsRunnable} implementation.</p>
 * 
 * <p>You may plugin custom implementations of this interface using 
 * the {@link java.util.ServiceLoader} mechanisms</p>
 * 
 * <p><strong>NOTE:</strong> this class is not part of camunda BPM platform 
 * public api and as such potentially to incompatible change.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public interface JobAcquisitionStrategy {
  
  /** this strategy iterates through the process engines and queries 
   * each process engine for jobs, one process engine at a time */
  public static String SEQUENTIAL = "SEQUENTIAL";
  
// TODO:
//  /** this strategy queries multile process engines at the same time */
//  public static String SIMULTANEOUS = "SIMULTANEOUS";

  public String getJobAcquisitionName();
  
  public AcquireJobsRunnable getAcquireJobsRunnable(JobExecutor jobAcquisition);
  
  
}
