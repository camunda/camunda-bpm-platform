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

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanService;
import org.camunda.bpm.engine.application.ProcessApplicationRegistration;

/**
 * 
 * @author Daniel Meyer
 *
 */
public class JmxProcessApplication extends MBeanService<ProcessApplicationReference> implements JmxProcessApplicationMBean {
	
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

	public ProcessApplicationReference getValue() {
		return processApplicationReference;
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
