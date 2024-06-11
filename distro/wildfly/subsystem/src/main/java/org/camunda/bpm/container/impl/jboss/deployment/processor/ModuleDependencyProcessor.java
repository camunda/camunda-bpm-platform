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

import java.util.function.Consumer;

import org.camunda.bpm.container.impl.jboss.deployment.marker.ProcessApplicationAttachments;
import org.camunda.bpm.container.impl.jboss.service.ProcessApplicationModuleService;
import org.camunda.bpm.container.impl.jboss.service.ServiceNames;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

/**
 * <p>This Processor creates implicit module dependencies for process applications</p>
 *
 * <p>Concretely speaking, this processor adds a module dependency from the process
 * application module (deployment unit) to the process engine module (and other camunda libraries
 * which are useful for process apps).</p>
 *
 * @author Daniel Meyer
 *
 */
public class ModuleDependencyProcessor implements DeploymentUnitProcessor {

  public static final int PRIORITY = 0x2300;

  public static String MODULE_IDENTIFIER_PROCESS_ENGINE = "org.camunda.bpm.camunda-engine";
  public static String MODULE_IDENTIFIER_XML_MODEL = "org.camunda.bpm.model.camunda-xml-model";
  public static String MODULE_IDENTIFIER_BPMN_MODEL = "org.camunda.bpm.model.camunda-bpmn-model";
  public static String MODULE_IDENTIFIER_CMMN_MODEL = "org.camunda.bpm.model.camunda-cmmn-model";
  public static String MODULE_IDENTIFIER_DMN_MODEL = "org.camunda.bpm.model.camunda-dmn-model";
  public static String MODULE_IDENTIFIER_SPIN = "org.camunda.spin.camunda-spin-core";
  public static String MODULE_IDENTIFIER_CONNECT = "org.camunda.connect.camunda-connect-core";
  public static String MODULE_IDENTIFIER_ENGINE_DMN = "org.camunda.bpm.dmn.camunda-engine-dmn";
  public static String MODULE_IDENTIFIER_GRAAL_JS = "org.graalvm.js.js-scriptengine";
  public static String MODULE_IDENTIFIER_JUEL = "org.camunda.bpm.juel.camunda-juel";

  @Override
  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

    if (deploymentUnit.getParent() == null) {
      //The deployment unit has no parent so it is a simple war or an ear.
      ModuleLoader moduleLoader = Module.getBootModuleLoader();
      //If it is a simpleWar and marked with process application we have to add the dependency
      boolean isProcessApplicationWarOrEar = ProcessApplicationAttachments.isProcessApplication(deploymentUnit);

      AttachmentList<DeploymentUnit> subdeployments = deploymentUnit.getAttachment(Attachments.SUB_DEPLOYMENTS);
      //Is the list of sub deployments empty the deployment unit is a war file.
      //In cases of war files we have nothing todo.
      if (subdeployments != null) {
        //The deployment unit contains sub deployments which means the deployment unit is an ear.
        //We have to check whether sub deployments are process applications or not.
        boolean subDeploymentIsProcessApplication = false;
        for (DeploymentUnit subDeploymentUnit : subdeployments) {
          if (ProcessApplicationAttachments.isProcessApplication(subDeploymentUnit)) {
            subDeploymentIsProcessApplication = true;
            break;
          }
        }
        //If one sub deployment is a process application then we add to all the dependency
        //Also we have to add the dependency to the current deployment unit which is an ear
        if (subDeploymentIsProcessApplication) {
          for (DeploymentUnit subDeploymentUnit : subdeployments) {
            final ModuleSpecification moduleSpecification = subDeploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
            addSystemDependencies(moduleLoader, moduleSpecification);
          }
          //An ear is not marked as process application but also needs the dependency
          isProcessApplicationWarOrEar = true;
        }
      }

      if (isProcessApplicationWarOrEar) {
        final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        addSystemDependencies(moduleLoader, moduleSpecification);
      }
    }

    // install the pa-module service
    ModuleIdentifier identifyer = deploymentUnit.getAttachment(Attachments.MODULE_IDENTIFIER);
    String moduleName = identifyer.toString();

    ServiceName serviceName = ServiceNames.forProcessApplicationModuleService(moduleName);

    ServiceBuilder<?> serviceBuilder = phaseContext.getServiceTarget().addService(serviceName);
    Consumer<ProcessApplicationModuleService> provider = serviceBuilder.provides(serviceName);
    serviceBuilder.requires(phaseContext.getPhaseServiceName());
    serviceBuilder.setInitialMode(Mode.ACTIVE);
    ProcessApplicationModuleService processApplicationModuleService = new ProcessApplicationModuleService(provider);
    serviceBuilder.setInstance(processApplicationModuleService);
    serviceBuilder.install();

  }

  private void addSystemDependencies(ModuleLoader moduleLoader, final ModuleSpecification moduleSpecification) {
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_PROCESS_ENGINE);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_XML_MODEL);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_BPMN_MODEL);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_CMMN_MODEL);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_DMN_MODEL);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_SPIN);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_CONNECT);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_ENGINE_DMN);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_GRAAL_JS, true);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFIER_JUEL, true);
  }

  private void addSystemDependency(ModuleLoader moduleLoader, final ModuleSpecification moduleSpecification, String identifier) {
    addSystemDependency(moduleLoader, moduleSpecification, identifier, false);
  }

  private void addSystemDependency(ModuleLoader moduleLoader, final ModuleSpecification moduleSpecification, String identifier, boolean importServices) {
    moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, identifier, false, false, importServices, false));
  }

}
