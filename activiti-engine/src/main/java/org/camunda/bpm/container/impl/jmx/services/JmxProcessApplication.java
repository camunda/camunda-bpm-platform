package org.camunda.bpm.container.impl.jmx.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.kernel.MbeanService;
import org.camunda.bpm.engine.application.ProcessApplicationRegistration;

/**
 * 
 * @author Daniel Meyer
 *
 */
public class JmxProcessApplication extends MbeanService<JmxProcessApplicationMBean> implements JmxProcessApplicationMBean {
	
	protected final ProcessApplicationReference processApplicationReference;
  protected List<ProcessesXml> processesXmls;
  private Map<String, ProcessApplicationRegistration> deploymentMap;
	
	public JmxProcessApplication(ProcessApplicationReference reference) {
    this.processApplicationReference = reference;
	}

	public String getProcessApplicationName() {
		return processApplicationReference.getName();
	}

	public void start(MBeanServiceContainer mBeanServiceContainer) {
	  	  
	}

	public void stop(MBeanServiceContainer mBeanServiceContainer) {
				
	}

	public JmxProcessApplicationMBean getValue() {
		return this;
	}

  public void setProcessesXmls(List<ProcessesXml> processesXmls) {
    this.processesXmls = processesXmls;
  }
  
  public List<ProcessesXml> getProcessesXmls() {
    return processesXmls;
  }

  public void setDeploymentMap(Map<String, ProcessApplicationRegistration> processArchiveDeploymentMap) {
    this.deploymentMap = processArchiveDeploymentMap;
  }
  
  public Map<String, ProcessApplicationRegistration> getProcessArchiveDeploymentMap() {
    return deploymentMap;
  }

  public List<String> getDeploymentIds() {
    List<String> deploymentIds = new ArrayList<String>();
    for (ProcessApplicationRegistration registration : deploymentMap.values()) {
      deploymentIds.add(registration.getDeploymentId());
    }
    return deploymentIds;
  }

  public List<String> getDeploymentNames() {
    return new ArrayList<String>(deploymentMap.keySet());
  }

}
