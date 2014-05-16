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
import org.camunda.bpm.application.impl.ProcessApplicationInfoImpl;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.deployment.util.DeployedProcessArchive;
import org.camunda.bpm.container.impl.spi.PlatformService;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;

/**
 *
 * @author Daniel Meyer
 *
 */
public class JmxManagedProcessApplication implements PlatformService<JmxManagedProcessApplication>, JmxManagedProcessApplicationMBean {

  protected final ProcessApplicationInfoImpl processApplicationInfo;
  protected final ProcessApplicationReference processApplicationReference;

  protected List<ProcessesXml> processesXmls;
  protected Map<String, DeployedProcessArchive> deploymentMap;

	public JmxManagedProcessApplication(ProcessApplicationInfoImpl processApplicationInfo, ProcessApplicationReference processApplicationReference) {
    this.processApplicationInfo = processApplicationInfo;
    this.processApplicationReference = processApplicationReference;
	}

	public String getProcessApplicationName() {
		return processApplicationInfo.getName();
	}

	public void start(PlatformServiceContainer mBeanServiceContainer) {
	}

	public void stop(PlatformServiceContainer mBeanServiceContainer) {
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

  public void setDeploymentMap(Map<String, DeployedProcessArchive> processArchiveDeploymentMap) {
    this.deploymentMap = processArchiveDeploymentMap;
  }

  public Map<String, DeployedProcessArchive> getProcessArchiveDeploymentMap() {
    return deploymentMap;
  }

  public List<String> getDeploymentIds() {
    List<String> deploymentIds = new ArrayList<String>();
    for (DeployedProcessArchive registration : deploymentMap.values()) {
      deploymentIds.addAll(registration.getAllDeploymentIds());
    }
    return deploymentIds;
  }

  public List<String> getDeploymentNames() {
    return new ArrayList<String>(deploymentMap.keySet());
  }

  public ProcessApplicationInfoImpl getProcessApplicationInfo() {
    return processApplicationInfo;
  }

  public ProcessApplicationReference getProcessApplicationReference() {
    return processApplicationReference;
  }

}
