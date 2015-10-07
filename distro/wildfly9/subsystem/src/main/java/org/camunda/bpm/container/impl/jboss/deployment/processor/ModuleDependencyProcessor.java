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

  public static ModuleIdentifier MODULE_IDENTIFYER_PROCESS_ENGINE = ModuleIdentifier.create("org.camunda.bpm.camunda-engine");
  public static ModuleIdentifier MODULE_IDENTIFYER_XML_MODEL = ModuleIdentifier.create("org.camunda.bpm.model.camunda-xml-model");
  public static ModuleIdentifier MODULE_IDENTIFYER_BPMN_MODEL = ModuleIdentifier.create("org.camunda.bpm.model.camunda-bpmn-model");
  public static ModuleIdentifier MODULE_IDENTIFYER_CMMN_MODEL = ModuleIdentifier.create("org.camunda.bpm.model.camunda-cmmn-model");
  public static ModuleIdentifier MODULE_IDENTIFYER_DMN_MODEL = ModuleIdentifier.create("org.camunda.bpm.model.camunda-dmn-model");
  public static ModuleIdentifier MODULE_IDENTIFYER_SPIN = ModuleIdentifier.create("org.camunda.spin.camunda-spin-core");
  public static ModuleIdentifier MODULE_IDENTIFYER_CONNECT = ModuleIdentifier.create("org.camunda.connect.camunda-connect-core");
  public static ModuleIdentifier MODULE_IDENTIFYER_ENGINE_DMN = ModuleIdentifier.create("org.camunda.dmn.camunda-engine-dmn");
  public static ModuleIdentifier MODULE_IDENTIFYER_SCRIPTENGINE_DMN = ModuleIdentifier.create("org.camunda.dmn.camunda-scriptengine-dmn");

  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

    final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }

    ModuleLoader moduleLoader = Module.getBootModuleLoader();
    DeploymentUnit parent = deploymentUnit.getParent() == null ? deploymentUnit : deploymentUnit.getParent();

    if(parent != deploymentUnit) {
      // add dependency to all submodules
      AttachmentList<DeploymentUnit> subdeployments = parent.getAttachment(Attachments.SUB_DEPLOYMENTS);
      for (DeploymentUnit subdeploymentUnit : subdeployments) {
        final ModuleSpecification moduleSpecification = subdeploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        addSystemDependencies(moduleLoader, moduleSpecification);
      }

    }

    final ModuleSpecification moduleSpecification = parent.getAttachment(Attachments.MODULE_SPECIFICATION);
    addSystemDependencies(moduleLoader, moduleSpecification);

    // install the pa-module service
    ModuleIdentifier identifyer = deploymentUnit.getAttachment(Attachments.MODULE_IDENTIFIER);
    String moduleName = identifyer.toString();

    ProcessApplicationModuleService processApplicationModuleService = new ProcessApplicationModuleService();
    ServiceName serviceName = ServiceNames.forProcessApplicationModuleService(moduleName);

    phaseContext.getServiceTarget()
      .addService(serviceName, processApplicationModuleService)
      .addDependency(phaseContext.getPhaseServiceName())
      .setInitialMode(Mode.ACTIVE)
      .install();

  }

  private void addSystemDependencies(ModuleLoader moduleLoader, final ModuleSpecification moduleSpecification) {
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFYER_PROCESS_ENGINE);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFYER_XML_MODEL);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFYER_BPMN_MODEL);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFYER_CMMN_MODEL);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFYER_DMN_MODEL);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFYER_SPIN);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFYER_CONNECT);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFYER_ENGINE_DMN);
    addSystemDependency(moduleLoader, moduleSpecification, MODULE_IDENTIFYER_SCRIPTENGINE_DMN);
  }

  private void addSystemDependency(ModuleLoader moduleLoader, final ModuleSpecification moduleSpecification, ModuleIdentifier dependency) {
    moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, dependency, false, false, false, false));
  }

  public void undeploy(DeploymentUnit context) {

  }

}
