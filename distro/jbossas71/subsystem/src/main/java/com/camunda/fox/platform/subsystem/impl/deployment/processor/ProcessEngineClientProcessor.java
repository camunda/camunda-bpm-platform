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

import javax.ejb.ConcurrencyManagementType;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;

import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.ProcessEngineClient;
import org.camunda.bpm.application.impl.EjbProcessApplication;
import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.EEApplicationClasses;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ejb3.component.singleton.SingletonComponentDescription;
import org.jboss.as.ejb3.deployment.ApplicationExceptionDescriptions;
import org.jboss.as.ejb3.deployment.EjbDeploymentAttachmentKeys;
import org.jboss.as.ejb3.deployment.EjbDeploymentMarker;
import org.jboss.as.ejb3.deployment.EjbJarDescription;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.metadata.ejb.jboss.ejb3.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.spec.ContainerTransactionMetaData;
import org.jboss.metadata.ejb.spec.EjbJarMetaData;
import org.jboss.metadata.ejb.spec.EjbJarVersion;
import org.jboss.metadata.ejb.spec.EjbType;
import org.jboss.metadata.ejb.spec.GenericBeanMetaData;
import org.jboss.metadata.ejb.spec.SessionType;

import com.camunda.fox.platform.subsystem.impl.deployment.marker.ProcessApplicationAttachments;

/**
 * This processor either looks up a user-provided @ProcessEngineClient component or 
 * or synthesizes a new component using default values. 
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessEngineClientProcessor implements DeploymentUnitProcessor {
  
  private final static Logger log = Logger.getLogger(ProcessEngineClientProcessor.class.getName());

  public static final int PRIORITY = 0x1150;

  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }

    // discover user-provided component
    SingletonComponentDescription clientComponent = detectExistingClientComponent(deploymentUnit); 

    if(clientComponent == null) {
      log.log(Level.INFO, "Could not detect @ProcessEngineClient component. Adding session bean with default configuration.");
      clientComponent = synthesizeClientComponent(deploymentUnit);      

    } else {
      log.log(Level.INFO, "Detected user-provided @ProcessEngineClient component with name '"+clientComponent.getComponentName()+"'.");

    }

    // attach the component such that it can be referenced later.
    ProcessApplicationAttachments.attachProcessEngineClientComponent(deploymentUnit, clientComponent);
       
  }

  /** 
   * Detect an existing {@link ProcessEngineClient} component.  
   */
  protected SingletonComponentDescription detectExistingClientComponent(DeploymentUnit deploymentUnit) throws DeploymentUnitProcessingException {
    
    final EEModuleDescription eeModuleDescription = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
    final ResourceRoot deploymentRoot = deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.DEPLOYMENT_ROOT);
    final Index annotationIndex = deploymentRoot.getAttachment(org.jboss.as.server.deployment.Attachments.ANNOTATION_INDEX);
    
    List<AnnotationInstance> annotations = annotationIndex.getAnnotations(DotName.createSimple(ProcessEngineClient.class.getName()));
    if(annotations.isEmpty()) {
      return null; // no component found
      
    } else if(annotations.size() > 1) {
      throw new DeploymentUnitProcessingException("Detected more than one class annotated with @" + ProcessEngineClient.class.getSimpleName()
          + ". A process application can only carry a single process engine client.");
      
    } else {
      AnnotationInstance annotationInstance = annotations.get(0);
      AnnotationTarget target = annotationInstance.target();
      
      if( !(target instanceof ClassInfo) ) {
        throw new DeploymentUnitProcessingException("@"+ProcessEngineClient.class.getSimpleName()+" annotation must be placed on a Type.");
      }
      
      ClassInfo clientComponentClassInfo = (ClassInfo) target;      
      String clientComponentClassName = clientComponentClassInfo.name().toString();
      
      List<ComponentDescription> componentsByClassName = eeModuleDescription.getComponentsByClassName(clientComponentClassName);
      if(componentsByClassName.isEmpty()) {
        throw new DeploymentUnitProcessingException("Class " + clientComponentClassName + " is annotated with @" + ProcessEngineClient.class.getSimpleName()
            + " but is not a @Singleton session bean.");
      } else {
        ComponentDescription componentDescription = componentsByClassName.get(0);
        if (componentDescription instanceof SingletonComponentDescription) {
          return (SingletonComponentDescription) componentDescription;
          
        } else {
          throw new DeploymentUnitProcessingException("Class " + clientComponentClassName + " is annotated with @" + ProcessEngineClient.class.getSimpleName()
              + " but is not a @Singleton session bean.");
          
        }
      }      
    }
  }

  protected SingletonComponentDescription synthesizeClientComponent(final DeploymentUnit deploymentUnit) {
    // create a synthetic ProcessApplication component. 
    EEModuleDescription eeModuleDescription = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
    EEApplicationClasses applicationClassesDescription = deploymentUnit.getAttachment(Attachments.EE_APPLICATION_CLASSES_DESCRIPTION);
    EjbJarMetaData ejbJarMetaData = deploymentUnit.getAttachment(EjbDeploymentAttachmentKeys.EJB_JAR_METADATA);
    EjbJarDescription ejbJarDescription = deploymentUnit.getAttachment(EjbDeploymentAttachmentKeys.EJB_JAR_DESCRIPTION);
    ApplicationExceptionDescriptions applicationExceptionDescriptions = deploymentUnit.getAttachment(EjbDeploymentAttachmentKeys.APPLICATION_EXCEPTION_DESCRIPTIONS);
    
    if(ejbJarMetaData == null) {
      ejbJarMetaData = new EjbJarMetaData(EjbJarVersion.EJB_3_1);
      deploymentUnit.putAttachment(EjbDeploymentAttachmentKeys.EJB_JAR_METADATA, ejbJarMetaData);
    }
    
    // turn this into an EJB Jar!
    if(ejbJarDescription == null) {
      ejbJarDescription = new EjbJarDescription(eeModuleDescription, applicationClassesDescription, deploymentUnit.getName().endsWith(".war"));
      deploymentUnit.putAttachment(EjbDeploymentAttachmentKeys.EJB_JAR_DESCRIPTION, ejbJarDescription);
      EjbDeploymentMarker.mark(deploymentUnit);
    }
    
    if(applicationExceptionDescriptions == null) {
      applicationExceptionDescriptions = new ApplicationExceptionDescriptions();
      deploymentUnit.putAttachment(EjbDeploymentAttachmentKeys.APPLICATION_EXCEPTION_DESCRIPTIONS, applicationExceptionDescriptions);
    }
    
    GenericBeanMetaData sessionBean = new GenericBeanMetaData(EjbType.SESSION);
    
    JBossEnterpriseBeansMetaData enterpriseBeansMetaData = new JBossEnterpriseBeansMetaData();
    enterpriseBeansMetaData.setEjbJarMetaData(ejbJarMetaData);
    sessionBean.setEnterpriseBeansMetaData(enterpriseBeansMetaData);
    
    sessionBean.setConcurrencyManagementType(ConcurrencyManagementType.BEAN);
    sessionBean.setSessionType(SessionType.Singleton);
    sessionBean.setEjbClass(EjbProcessApplication.class.getName());
    // transactions (TransactionAttribute.SUPPORTS)
    sessionBean.setTransactionType(TransactionManagementType.CONTAINER);
    ContainerTransactionMetaData containerTransactionMetaData = new ContainerTransactionMetaData();
    containerTransactionMetaData.setTransAttribute(TransactionAttributeType.SUPPORTS);
    
    SingletonComponentDescription processApplicationComponent = new SingletonComponentDescription("EjbProcessApplication", 
        EjbProcessApplication.class.getName(), 
        ejbJarDescription, deploymentUnit.getServiceName(), sessionBean);
    
    // add the process application component
    eeModuleDescription.addComponent(processApplicationComponent);
    
    // turn ProcessApplicationExecutionException into an EJB ApplicationExection
    // to make sure the container does not rollback the transaction prematurely
    applicationExceptionDescriptions.addApplicationException(ProcessApplicationExecutionException.class.getName(), false, true);
    
    return processApplicationComponent;
  }

  @Override
  public void undeploy(DeploymentUnit context) {
    
  }

}
