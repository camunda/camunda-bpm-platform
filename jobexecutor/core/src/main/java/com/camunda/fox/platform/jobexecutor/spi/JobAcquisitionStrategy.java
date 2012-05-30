package com.camunda.fox.platform.jobexecutor.spi;

import org.activiti.engine.impl.jobexecutor.AcquireJobsRunnable;

import com.camunda.fox.platform.jobexecutor.impl.acquisition.JobAcquisition;

/**
 * <p>A job acquisition strategy implements the 'active' part of a 
 * job Acquisition. A strategy is characterized by a (unique) name 
 * and a custom {@link AcquireJobsRunnable} implementation.</p>
 * 
 * <p>You may plugin custom implementations of this interface using 
 * the {@link java.util.ServiceLoader} mechanisms</p>
 * 
 * <p><strong>NOTE:</strong> this class is not part of fox platform 
 * public api and as such potentially to incompatible change.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public interface JobAcquisitionStrategy {
  
  /** this strategy iterates through the process engines and queries 
   * each process engine for jobs, one process engine at a time */
  public static String SEQENTIAL = "SEQUENTIAL";
  
// TODO:
//  /** this strategy queries multile process engines at the same time */
//  public static String SIMULTANEOUS = "SIMULTANEOUS";

  public String getJobAcquisitionName();
  
  public AcquireJobsRunnable getAcquireJobsRunnable(JobAcquisition jobAcquisition);
  
  
}
