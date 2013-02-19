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
package com.camunda.fox.platform.subsystem.impl.deployment.marker;

import java.util.Map;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.deployment.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.deployment.spi.ProcessesXml;
import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;

/**
 * 
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationAttachments {

  private static final AttachmentKey<Boolean> MARKER = AttachmentKey.create(Boolean.class);
  private static final AttachmentKey<ComponentDescription> PA_COMPONENT = AttachmentKey.create(ComponentDescription.class);
  private static final AttachmentKey<Map<ProcessArchiveXml, String>> DEPLOYMENT_MAP = AttachmentKey.create(Map.class);
  private static final AttachmentKey<ProcessesXml> PROCESSES_XML = AttachmentKey.create(ProcessesXml.class);

  /**
   * Attach the parsed ProcessesXml file to a deployment unit.
   *  
   */
  public static void attachProcessesXml(DeploymentUnit unit, ProcessesXml processesXml) {
      unit.putAttachment(PROCESSES_XML, processesXml);
  }

  /**
   * Returns the attached {@link ProcessesXml} marker or null;
   *  
   */
  public static ProcessesXml getProcessesXml(DeploymentUnit deploymentUnit) {
    return deploymentUnit.getAttachment(PROCESSES_XML);
  }

  /** 
   * marks a a {@link DeploymentUnit} as a process application 
   */
  public static void mark(DeploymentUnit unit) {
      unit.putAttachment(MARKER, Boolean.TRUE);
  }
  
  /**
   * Returns true if the {@link DeploymentUnit} itself is a process application (carries a processes.xml)
   * 
   */
  public static boolean isProcessApplication(DeploymentUnit deploymentUnit) {
    return deploymentUnit.hasAttachment(MARKER);
  }

  /**
   * Returns true if the {@link DeploymentUnit} is a process application or part
   * of a process application.
   * 
   */
  public static boolean isPartOfProcessApplication(DeploymentUnit deploymentUnit) {
    return isProcessApplication(deploymentUnit) ||
       ( deploymentUnit.getParent() != null && deploymentUnit.getParent().hasAttachment(MARKER) );
  }
  
  /**
   * Returns the {@link ComponentDescription} for the {@link ProcessApplication} component
   */
  public static ComponentDescription getProcessApplicationComponent(DeploymentUnit deploymentUnit) {
    return deploymentUnit.getAttachment(PA_COMPONENT);
  }
  
  /**
   * Attach the {@link ComponentDescription} for the {@link ProcessApplication} component   
   */
  public static void attachProcessEngineClientComponent(DeploymentUnit deploymentUnit, ComponentDescription componentDescription){
    deploymentUnit.putAttachment(PA_COMPONENT, componentDescription);
  }
  
  /**
   * Attach a {@link Map<ParsedProcessArchive, ProcessApplicationRegistration>} to the deployment
   */
  public static void attachDeploymentMap(DeploymentUnit deploymentUnit, Map<ProcessArchiveXml, String> deploymentMap) {
    deploymentUnit.putAttachment(DEPLOYMENT_MAP, deploymentMap);
  }

  /**
   * Returns the deployment map for the {@link DeploymentUnit}
   */
  public static Map<ProcessArchiveXml, String> getDeploymentMap(DeploymentUnit deploymentUnit) {
    return deploymentUnit.getAttachment(DEPLOYMENT_MAP);    
  }
  
  private ProcessApplicationAttachments() {

  }
}
