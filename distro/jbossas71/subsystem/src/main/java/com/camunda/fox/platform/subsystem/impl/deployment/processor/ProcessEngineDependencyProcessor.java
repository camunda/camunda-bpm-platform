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
package com.camunda.fox.platform.subsystem.impl.deployment.processor;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;

import com.camunda.fox.platform.subsystem.impl.deployment.marker.ProcessApplicationAttachments;
import com.camunda.fox.platform.subsystem.impl.deployment.marker.ProcessEngineDependencyAttachements;
import com.camunda.fox.platform.subsystem.impl.service.ManagedProcessEngineController;

/**
 * <p>This processor adds the service dependencies to the process engines declared in the <code>processes.xml</code> file.</p>  
 * 
 * @author Daniel Meyer
 *
 */
public class ProcessEngineDependencyProcessor implements DeploymentUnitProcessor {
  
  public static final int PRIORITY = 0x2301;

  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    
    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
    
    if(!ProcessApplicationAttachments.isPartOfProcessApplication(deploymentUnit)) {
      return;
    }
    
    final ProcessesXml processesXml = ProcessApplicationAttachments.getProcessesXml(deploymentUnit).getProcessesXml();
    
    boolean isDefaultEngineReferenced = false;
    Set<String> referencedProcessEngines = new HashSet<String>();
    
    for (ProcessArchiveXml parsedProcessArchive : processesXml.getProcessArchives()) {      
      String processEngineName = parsedProcessArchive.getProcessEngineName();      
      if(processEngineName == null || processEngineName.length()==0) {
        isDefaultEngineReferenced = true;        
      } else {
        referencedProcessEngines.add(processEngineName);
      }
    }
    
    if(isDefaultEngineReferenced) {
      // add dependency to the default engine
      phaseContext.addDeploymentDependency(ManagedProcessEngineController.createServiceNameForDefaultEngine(), 
          ProcessEngineDependencyAttachements.getDefaultEngineKey());
    }
    
    for (String engineName : referencedProcessEngines) {
      // add dependencies to the services representing the process engines. 
      phaseContext.addDeploymentDependency(ManagedProcessEngineController.createServiceName(engineName), 
          ProcessEngineDependencyAttachements.getDependentEnginesKey());
    }
    
  }

  public void undeploy(DeploymentUnit context) {

  }

}
