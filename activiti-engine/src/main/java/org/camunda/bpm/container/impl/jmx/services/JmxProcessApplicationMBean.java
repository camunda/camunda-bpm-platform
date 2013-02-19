package org.camunda.bpm.container.impl.jmx.services;

import java.util.List;

import org.camunda.bpm.application.ProcessApplication;

/**
 * MBean interface for {@link ProcessApplication ProcessApplications}. This
 * interface allows retrieving management information about process
 * applications.
 * 
 * @author Daniel Meyer
 * 
 */
public interface JmxProcessApplicationMBean {
	
  /**
   * @return the name of the process application
   */
	public String getProcessApplicationName();
	
	/** 
	 * @return the ids of the process engine deployments performed by this application
	 */
	public List<String> getDeploymentIds();
	
	/**
	 * @return the names of the deployments performed by this application
	 */
	public List<String> getDeploymentNames();
	

}
