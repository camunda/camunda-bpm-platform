package org.camunda.bpm.application.impl.deployment.metadata.spi;

import java.util.Map;

import com.camunda.fox.platform.jobexecutor.spi.JobAcquisitionStrategy;

/**
 * <p>Java API to the JobAcquisition deployment metadata</p>
 * 
 * @author Daniel Meyer
 * 
 */
public interface JobAcquisitionXml {

  public final static String LOCK_TIME_IN_MILLIS = "lockTimeInMillis";
  public final static String WAIT_TIME_IN_MILLIS = "lockTimeInMillis";
  public final static String MAX_JOBS_PER_ACQUISITION = "maxJobsPerAcquisition";

  /**
   * @return the name of the JobAcquisition.
   */
  public String getName();

  /**
   * @return the name of the Job Acquisition Strategy. If unspecified, the
   *         default value is {@link JobAcquisitionStrategy#SEQUENTIAL}.
   */
  public String getAcquisitionStrategy();

  /**
   * @return a set of properties to configure the Job Acquisition. The
   *         properties are mapped to bean properties of the JobAcquisition
   *         class used.
   * 
   * @see #LOCK_TIME_IN_MILLIS
   * @see #WAIT_TIME_IN_MILLIS
   * @see #MAX_JOBS_PER_ACQUISITION
   * 
   */
  public Map<String, String> getProperties();

}
