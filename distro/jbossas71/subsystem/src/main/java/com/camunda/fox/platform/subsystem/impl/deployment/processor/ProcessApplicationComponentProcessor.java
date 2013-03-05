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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplication;
import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ejb3.component.singleton.SingletonComponentDescription;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;

import com.camunda.fox.platform.subsystem.impl.deployment.marker.ProcessApplicationAttachments;

/**
 * <p>This processor detects a user-provided component annotated with the {@link ProcessApplication}-annotation.</p>
 * 
 * <p>If no such component is found but the deployment unit carries a META-INF/processes.xml file, a 
 * Singleton Session Bean component is synthesized.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessApplicationComponentProcessor implements DeploymentUnitProcessor {
  
  private final static Logger log = Logger.getLogger(ProcessApplicationComponentProcessor.class.getName());

  public static final int PRIORITY = 0x1151;

  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();    
    final EEModuleDescription eeModuleDescription = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
    
    // must be EE Module
    if(eeModuleDescription == null) {
      return;
    }

    // discover user-provided component
    SingletonComponentDescription clientComponent = detectExistingComponent(deploymentUnit); 

    if(clientComponent != null) {      
      log.log(Level.INFO, "Detected user-provided @"+ProcessApplication.class.getSimpleName()+" component with name '"+clientComponent.getComponentName()+"'.");
      
      // mark this to be a process application
      ProcessApplicationAttachments.mark(deploymentUnit);
      ProcessApplicationAttachments.markPartOfProcessApplication(deploymentUnit);
      // attach description to the deployment unit
      ProcessApplicationAttachments.attachProcessApplicationComponent(deploymentUnit, clientComponent);
    }
  }

  /** 
   * Detect an existing {@link ProcessApplication} component.  
   */
  protected SingletonComponentDescription detectExistingComponent(DeploymentUnit deploymentUnit) throws DeploymentUnitProcessingException {
    
    final EEModuleDescription eeModuleDescription = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
    final ResourceRoot deploymentRoot = deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.DEPLOYMENT_ROOT);
    final Index annotationIndex = deploymentRoot.getAttachment(org.jboss.as.server.deployment.Attachments.ANNOTATION_INDEX);
    final CompositeIndex compositeIndex = deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.COMPOSITE_ANNOTATION_INDEX);
    
    List<AnnotationInstance> annotations = null;    
    if(compositeIndex != null) {
      annotations = compositeIndex.getAnnotations(DotName.createSimple(ProcessApplication.class.getName()));
      
    } else {   
      annotations = annotationIndex.getAnnotations(DotName.createSimple(ProcessApplication.class.getName()));
      
    }
    
    if(annotations.isEmpty()) {
      return null; // no component found
      
    } else if(annotations.size() > 1) {
      throw new DeploymentUnitProcessingException("Detected more than one class annotated with @" + ProcessApplication.class.getSimpleName()
          + ". A process application can only carry a single process engine client.");
      
    } else {
      AnnotationInstance annotationInstance = annotations.get(0);
      AnnotationTarget target = annotationInstance.target();
      
      if( !(target instanceof ClassInfo) ) {
        throw new DeploymentUnitProcessingException("@"+ProcessApplication.class.getSimpleName()+" annotation must be placed on a Type.");
      }
      
      ClassInfo clientComponentClassInfo = (ClassInfo) target;      
      String clientComponentClassName = clientComponentClassInfo.name().toString();
      
      List<ComponentDescription> componentsByClassName = eeModuleDescription.getComponentsByClassName(clientComponentClassName);
      if(componentsByClassName.isEmpty()) {
        throw new DeploymentUnitProcessingException("Class " + clientComponentClassName + " is annotated with @" + ProcessApplication.class.getSimpleName()
            + " but is not a @Singleton session bean.");
      } else {
        ComponentDescription componentDescription = componentsByClassName.get(0);
        if (componentDescription instanceof SingletonComponentDescription) {
          return (SingletonComponentDescription) componentDescription;
          
        } else {
          throw new DeploymentUnitProcessingException("Class " + clientComponentClassName + " is annotated with @" + ProcessApplication.class.getSimpleName()
              + " but is not a @Singleton session bean.");
          
        }
      }      
    }
  }

  @Override
  public void undeploy(DeploymentUnit context) {
    
  }

}
