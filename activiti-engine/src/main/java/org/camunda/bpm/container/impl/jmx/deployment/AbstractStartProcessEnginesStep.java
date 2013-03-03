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
package org.camunda.bpm.container.impl.jmx.deployment;

import java.util.List;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;

/**
 * <p>Deployment operation step that is responsible for starting all process
 * engines declared in a {@link List} of {@link ProcessEngineXml} files.</p>
 * 
 * <p>This step does not start the process engines directly but rather creates
 * individual {@link StartProcessEngineStep} instances that each start a process
 * engine.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public abstract class AbstractStartProcessEnginesStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Start process engines";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {

    List<ProcessEngineXml> processEngines = getProcessEnginesXmls(operationContext);

    for (ProcessEngineXml parsedProcessEngine : processEngines) {
      // for each process engine add a new deployment step
      operationContext.addStep(new StartProcessEngineStep(parsedProcessEngine));
    }

  }

  protected abstract List<ProcessEngineXml> getProcessEnginesXmls(MBeanDeploymentOperation operationContext);

}
