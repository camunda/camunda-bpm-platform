/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jmx.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationDeploymentInfo;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.application.impl.ProcessApplicationDeploymentInfoImpl;
import org.camunda.bpm.application.impl.ProcessApplicationInfoImpl;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanService;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * 
 * @author Daniel Meyer
 *
 */
public class JmxManagedProcessApplication implements MBeanService<JmxManagedProcessApplication>, JmxManagedProcessApplicationMBean {
  
  private final Logger LOGGER = Logger.getLogger(JmxManagedProcessApplication.class.getName());
	
	protected ProcessApplicationReference processApplicationReference;
  protected List<ProcessesXml> processesXmls;
  protected Map<String, ProcessApplicationRegistration> deploymentMap;
  
  protected ProcessApplicationInfoImpl processApplicationInfo;
	
	public JmxManagedProcessApplication(ProcessApplicationReference reference) {
    this.processApplicationReference = reference;
	}

	public String getProcessApplicationName() {
		return processApplicationInfo.getName();
	}

	public void start(MBeanServiceContainer mBeanServiceContainer) {
	  
	  // populate process application info	  
	  processApplicationInfo = new ProcessApplicationInfoImpl();

    // set properties
    try {
      // set name
      AbstractProcessApplication processApplication = processApplicationReference.getProcessApplication();
      processApplicationInfo.setName(processApplication.getName());	  
      processApplicationInfo.setProperties(processApplication.getProperties());
    } catch (ProcessApplicationUnavailableException e) {
      throw new ProcessEngineException("Exception while starting process application service");
    }
	  
	  // create deployment infos
	  List<ProcessApplicationDeploymentInfo> deploymentInfoList = new ArrayList<ProcessApplicationDeploymentInfo>();	
	  if(deploymentMap != null) {
  	  for (Entry<String, ProcessApplicationRegistration> deployment : deploymentMap.entrySet()) {
  	    
        ProcessApplicationDeploymentInfoImpl deploymentInfo = new ProcessApplicationDeploymentInfoImpl();
        deploymentInfo.setDeploymentId(deployment.getValue().getDeploymentId());
        deploymentInfo.setDeploymentName(deployment.getKey());
        deploymentInfo.setProcessEngineName(deployment.getValue().getProcessEngineName());
        
        deploymentInfoList.add(deploymentInfo);
        
      }
	  }
	  
	  processApplicationInfo.setDeploymentInfo(deploymentInfoList);
	  
	  // clear reference
	  processApplicationReference = null;
	  
	  LOGGER.info("Process Application "+processApplicationInfo.getName()+" sucessfully deployed.");	  	  
	}

	public void stop(MBeanServiceContainer mBeanServiceContainer) {	  
	  processApplicationInfo = null;				
	}

	public JmxManagedProcessApplication getValue() {
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
  
  public ProcessApplicationInfoImpl getProcessApplicationInfo() {
    return processApplicationInfo;
  }

}
