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
package org.camunda.bpm.container.impl.jmx.kernel.util;

import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;

public class StopServiceDeploymentOperationStep extends DeploymentOperationStep {

  private String serviceName;

  public StopServiceDeploymentOperationStep(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getName() {
    return "stop "+serviceName;
  }

  public void performOperationStep(DeploymentOperation operationContext) {
    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();

    serviceContainer.stopService(serviceName);
  }

}
