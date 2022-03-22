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
package org.camunda.bpm.engine.test.junit5.deployment;

import org.camunda.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ProcessEngineExtensionDeploymentIdAccess {

  @Test
  public void testDeploymentIdWriteableForExtensions() {

    class ProcessEngineExtensionExtension extends ProcessEngineExtension {

      @Override
      public void beforeTestExecution(ExtensionContext context) {
        // here I could decide to override the way the deployment is created, e.g.
        // add mocked call activities etc, therefore I need deploymentId accessible
        // Note: in case the write access is removed, this will not compile anymore
        this.deploymentId = "xyz";
      }

    }

  }


  @Test
  public void testDeploymentIdReadableForExtensionsAndWrappers() {

    class ProcessEngineExtensionExtension extends ProcessEngineExtension {

      @Override
      public void afterTestExecution(ExtensionContext context) {
        // here I could decide to override the way the deployment is removed
        // Note: in case the write access is removed, this will not compile anymore
        String deploymentId = this.deploymentId;
      }

    }

    class ProcessEngineExtensionWrapper {

      ProcessEngineExtension wrapped = ProcessEngineExtension.builder().build();

      {
        // here I could decide to access the deployment. e.g. to look at the
        // runtime or history data of the current deployment before it is removed
        // Note: in case this read access is removed, this will not compile anymore
        String deploymentId = wrapped.getDeploymentId();
      }

    }

  }

}
