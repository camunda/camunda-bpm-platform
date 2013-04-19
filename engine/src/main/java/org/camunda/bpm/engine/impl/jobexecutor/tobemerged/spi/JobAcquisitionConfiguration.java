package org.camunda.bpm.engine.impl.jobexecutor.tobemerged.spi;

import java.util.Map;

/**
 * <p>Configuraiton of a job acquisition.</p> 
 * 
 * <p><strong>NOTE:</strong> this class is not part of camunda BPM platform 
 * public api and as such subject to incompatible change.</p>
 *  
 * @author Daniel Meyer
 */
public interface JobAcquisitionConfiguration {

  public final static String PROP_LOCK_TIME_IN_MILLIS = "lockTimeInMillis";
  public final static String PROP_WAIT_TIME_IN_MILLIS = "waitTimeInMillis";
  public final static String PROP_MAX_JOBS_PER_ACQUISITION = "maxJobsPerAcquisition";

  /** 
   * @return the name of a job acquisition. Must be unique for a given job executor instance 
   */
  public String getAcquisitionName();
  
  /** 
   * @return the name of the {@link JobAcquisitionStrategy} to use.
   * 
   * @see JobAcquisitionStrategy for a set of predefined strategies 
   */
  public String getJobAcquisitionStrategy();

  /**
   * @return additional properties of a job acquisition.
   * 
   * @see #PROP_LOCK_TIME_IN_MILLIS
   * @see #PROP_MAX_JOBS_PER_ACQUISITION
   * @see #PROP_WAIT_TIME_IN_MILLIS
   */
  Map<String, Object> getJobAcquisitionProperties();

}
