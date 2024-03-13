/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jboss.deployment.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.JakartaServletProcessApplication;
import org.camunda.bpm.container.impl.jboss.deployment.marker.ProcessApplicationAttachments;
import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.EEApplicationClasses;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ejb3.component.session.SessionBeanComponentDescription;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.as.web.common.WebComponentDescription;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;


/**
 * <p>This processor detects a user-provided component annotated with the {@link ProcessApplication}-annotation.</p>
 *
 * <p>If no such component is found but the deployment unit carries a META-INF/processes.xml file, a
 * Singleton Session Bean component is synthesized.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationProcessor implements DeploymentUnitProcessor {

  private final static Logger log = Logger.getLogger(ProcessApplicationProcessor.class.getName());

  public static final int PRIORITY = 0x2010; // after PARSE_WEB_MERGE_METADATA

  @Override
  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
    final EEModuleDescription eeModuleDescription = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);

    // must be EE Module
    if(eeModuleDescription == null) {
      return;
    }

    // discover user-provided component
    ComponentDescription paComponent = detectExistingComponent(deploymentUnit);

    if(paComponent != null) {
      log.log(Level.INFO, "Detected user-provided @"+ProcessApplication.class.getSimpleName()+" component with name '"+paComponent.getComponentName()+"'.");

      // mark this to be a process application
      ProcessApplicationAttachments.attachProcessApplicationComponent(deploymentUnit, paComponent);
      ProcessApplicationAttachments.mark(deploymentUnit);
      ProcessApplicationAttachments.markPartOfProcessApplication(deploymentUnit);
    }
  }

  /**
   * Detect an existing {@link ProcessApplication} component.
   */
  protected ComponentDescription detectExistingComponent(DeploymentUnit deploymentUnit) throws DeploymentUnitProcessingException {

    final EEModuleDescription eeModuleDescription = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
    final EEApplicationClasses eeApplicationClasses = deploymentUnit.getAttachment(Attachments.EE_APPLICATION_CLASSES_DESCRIPTION);
    final CompositeIndex compositeIndex = deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.COMPOSITE_ANNOTATION_INDEX);

    final WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);

    // extract deployment metadata
    List<AnnotationInstance> processApplicationAnnotations = null;
    List<AnnotationInstance> postDeployAnnnotations = null;
    List<AnnotationInstance> preUndeployAnnnotations = null;
    Set<ClassInfo> servletProcessApplications = null;
    Set<ClassInfo> unsupportedClasses = null;

    if(compositeIndex != null) {
      // allow coexistence of Javax- and Jakarta-based servlet process applications in deployments but only consider Jakarta-based ones here
      unsupportedClasses = compositeIndex.getAllKnownSubclasses(DotName.createSimple("org.camunda.bpm.application.impl.ServletProcessApplication"));
      processApplicationAnnotations = getAnnotationsFromSupportedClasses(compositeIndex, ProcessApplication.class, unsupportedClasses);
      postDeployAnnnotations = getAnnotationsFromSupportedClasses(compositeIndex, PostDeploy.class, unsupportedClasses);
      preUndeployAnnnotations = getAnnotationsFromSupportedClasses(compositeIndex, PreUndeploy.class, unsupportedClasses);
      servletProcessApplications = compositeIndex.getAllKnownSubclasses(DotName.createSimple(JakartaServletProcessApplication.class.getName()));
    } else {
      return null;
    }

    if(processApplicationAnnotations.isEmpty()) {
      // no pa found, this is not a process application deployment.
      return null;

    } else if(processApplicationAnnotations.size() > 1) {
      // found multiple PAs -> unsupported.
      throw new DeploymentUnitProcessingException("Detected multiple classes annotated with @" + ProcessApplication.class.getSimpleName()
          + ". A deployment must only provide a single @" + ProcessApplication.class.getSimpleName()
          + " class.");

    } else {
      // found single PA

      AnnotationInstance annotationInstance = processApplicationAnnotations.get(0);
      ClassInfo paClassInfo = (ClassInfo) annotationInstance.target();
      String paClassName = paClassInfo.name().toString();

      ComponentDescription paComponent = null;

      // it can either be a Servlet Process Application or a Singleton Session Bean Component or
      if(servletProcessApplications.contains(paClassInfo)) {

        // Servlet Process Applications can only be deployed inside Web Applications
        if(warMetaData == null) {
          throw new DeploymentUnitProcessingException("@ProcessApplication class is a JakartaServletProcessApplication but deployment is not a Web Application.");
        }

        // check whether it's already a servlet context listener:
        JBossWebMetaData mergedJBossWebMetaData = warMetaData.getMergedJBossWebMetaData();
        List<ListenerMetaData> listeners = mergedJBossWebMetaData.getListeners();
        if(listeners == null) {
          listeners = new ArrayList<ListenerMetaData>();
          mergedJBossWebMetaData.setListeners(listeners);
        }

        boolean isListener = false;
        for (ListenerMetaData listenerMetaData : listeners) {
          if(listenerMetaData.getListenerClass().equals(paClassInfo.name().toString())) {
            isListener = true;
          }
        }

        if(!isListener) {
          // register as Servlet Context Listener
          ListenerMetaData listener = new ListenerMetaData();
          listener.setListenerClass(paClassName);
          listeners.add(listener);

          // synthesize WebComponent
          WebComponentDescription paWebComponent = new WebComponentDescription(paClassName,
              paClassName,
              eeModuleDescription,
              deploymentUnit.getServiceName(),
              eeApplicationClasses);

          eeModuleDescription.addComponent(paWebComponent);

          deploymentUnit.addToAttachmentList(WebComponentDescription.WEB_COMPONENTS, paWebComponent.getStartServiceName());

          paComponent = paWebComponent;

        } else {
          // lookup the existing component
          paComponent = eeModuleDescription.getComponentsByClassName(paClassName).get(0);
        }

        // deactivate sci


      } else {

        // if its not a JakartaServletProcessApplication it must be a session bean component

        List<ComponentDescription> componentsByClassName = eeModuleDescription.getComponentsByClassName(paClassName);

        if (!componentsByClassName.isEmpty() && (componentsByClassName.get(0) instanceof SessionBeanComponentDescription)) {
          paComponent = componentsByClassName.get(0);

        } else {
          throw new DeploymentUnitProcessingException("Class " + paClassName + " is annotated with @" + ProcessApplication.class.getSimpleName()
              + " but is neither a JakartaServletProcessApplication nor an EJB Session Bean Component.");

        }

      }

      // attach additional metadata to the deployment unit

      if(!postDeployAnnnotations.isEmpty()) {
        if(postDeployAnnnotations.size()==1) {
          ProcessApplicationAttachments.attachPostDeployDescription(deploymentUnit, postDeployAnnnotations.get(0));
        } else {
          throw new DeploymentUnitProcessingException("There can only be a single method annotated with @PostDeploy. Found ["+postDeployAnnnotations+"]");
        }
      }

      if(!preUndeployAnnnotations.isEmpty()) {
        if(preUndeployAnnnotations.size()==1) {
          ProcessApplicationAttachments.attachPreUndeployDescription(deploymentUnit, preUndeployAnnnotations.get(0));
        } else {
          throw new DeploymentUnitProcessingException("There can only be a single method annotated with @PreUndeploy. Found ["+preUndeployAnnnotations+"]");
        }
      }

      return paComponent;
    }
  }

  protected List<AnnotationInstance> getAnnotationsFromSupportedClasses(CompositeIndex compositeIndex, Class<?> annotationClass, Set<ClassInfo> unsupportedClasses) {
    List<AnnotationInstance> annotations = compositeIndex.getAnnotations(DotName.createSimple(annotationClass.getName()));
    return annotations.stream()
        .filter(annotation -> {
          ClassInfo classInfo = getClassInfo(annotation);
          return classInfo == null || !unsupportedClasses.contains(classInfo);
        })
        .collect(Collectors.toList());
  }

  protected ClassInfo getClassInfo(AnnotationInstance annotation) {
    AnnotationTarget target = annotation.target();
    switch (target.kind()) {
    case METHOD:
      return target.asMethod().declaringClass();
    case CLASS:
      return target.asClass();
    default:
      return null;
    }
  }

}
