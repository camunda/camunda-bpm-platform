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
package org.camunda.bpm.container.impl.jboss.deployment.processor;

import static org.jboss.as.server.deployment.Attachments.DEPLOYMENT_ROOT;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ConcurrencyManagementType;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.impl.EjbProcessApplication;
import org.camunda.bpm.container.impl.jboss.deployment.marker.ProcessApplicationAttachments;
import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.EEApplicationClasses;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ejb3.component.singleton.SingletonComponentDescription;
import org.jboss.as.ejb3.deployment.ApplicationExceptionDescriptions;
import org.jboss.as.ejb3.deployment.EjbDeploymentAttachmentKeys;
import org.jboss.as.ejb3.deployment.EjbJarDescription;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.EjbDeploymentMarker;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.metadata.ejb.jboss.ejb3.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.spec.ContainerTransactionMetaData;
import org.jboss.metadata.ejb.spec.EjbJarMetaData;
import org.jboss.metadata.ejb.spec.EjbJarVersion;
import org.jboss.metadata.ejb.spec.EjbType;
import org.jboss.metadata.ejb.spec.GenericBeanMetaData;
import org.jboss.metadata.ejb.spec.SessionType;
import org.jboss.vfs.VirtualFile;


/**
 * <p>This processor synthesizes a Singleton Session Bean {@link EjbProcessApplication} component, if
 * <ul>
 *  <li>the deployment unit itself carries a META-INF/processes.xml file 
 *      (This means that if the META-INF/processes.xml is an Application library 
 *      packaged inside an EAR, we DO NOT synthesize a component because we 
 *      would not know which EE module to add the component to.)</li>
 *  <li>there is no user-provided ProcessApplication component present in the deployment. 
 *      (This restriction takes into account composite EAR deployments. In an EAR deployment,
 *      we only synthesize a component if ).</li>
 * </ul>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationComponentAddProcessor implements DeploymentUnitProcessor {
  
  private final static Logger LOGGER = Logger.getLogger(ProcessApplicationComponentAddProcessor.class.getName());
  
  // this must happen after we looked through the whole deployment for user-provided components. 
  public static final int PRIORITY = 0x1152;
  
  // indicates whether this step should be skipped.
  protected boolean skip;

  public ProcessApplicationComponentAddProcessor(boolean skip) {
    this.skip = skip;
  }
  
    
  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    
    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
    
    // do not proceed if this is already detected to be a Process Application 
    // (=>component exists either in this deployment unit or in a sibling)
    if(skip || ProcessApplicationAttachments.isPartOfProcessApplication(deploymentUnit)) {
      return;
    }
               
    if(processesXmlPresent(deploymentUnit)){   
      LOGGER.log(Level.INFO, "Could not detect @"+ProcessApplication.class.getSimpleName()+" component in deployment unit "+deploymentUnit.getName()+". Adding singleton session bean with default configuration.");
      SingletonComponentDescription clientComponent = synthesizeComponent(deploymentUnit);      
      
      // mark this to be a process application
      ProcessApplicationAttachments.mark(deploymentUnit);
      // attach description to the deployment unit
      ProcessApplicationAttachments.attachProcessApplicationComponent(deploymentUnit, clientComponent);
    } 

  }
  
  /**
   * TODO: should we really do this?
   */
  protected SingletonComponentDescription synthesizeComponent(final DeploymentUnit deploymentUnit) {
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
  
  protected boolean processesXmlPresent(DeploymentUnit deploymentUnit) {
    ResourceRoot resourceRoot = deploymentUnit.getAttachment(DEPLOYMENT_ROOT);    
    VirtualFile rootFile = resourceRoot.getRoot();
    
    // check jar
    VirtualFile processesXml = rootFile.getChild("META-INF/processes.xml");
    if(processesXml != null && processesXml.exists()) {
      return true;
    }
    
    // check war
    processesXml = rootFile.getChild("WEB-INF/classes/META-INF/processes.xml");
    return processesXml != null && processesXml.exists();    
  }

  
  public void undeploy(DeploymentUnit context) {
    // TODO Auto-generated method stub

  }

}
