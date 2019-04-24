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
package org.camunda.bpm.container.impl.deployment;

import java.util.List;

import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;

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
public abstract class AbstractStartProcessEnginesStep extends DeploymentOperationStep {

  public String getName() {
    return "Start process engines";
  }

  public void performOperationStep(DeploymentOperation operationContext) {

    List<ProcessEngineXml> processEngines = getProcessEnginesXmls(operationContext);

    for (ProcessEngineXml parsedProcessEngine : processEngines) {
      // for each process engine add a new deployment step
      operationContext.addStep(createStartProcessEngineStep(parsedProcessEngine));
    }

  }

  protected StartProcessEngineStep createStartProcessEngineStep(ProcessEngineXml parsedProcessEngine) {
    return new StartProcessEngineStep(parsedProcessEngine);
  }

  protected abstract List<ProcessEngineXml> getProcessEnginesXmls(DeploymentOperation operationContext);

}
