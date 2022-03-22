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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomProcessEngineExtension extends ProcessEngineExtension {

  private static final Logger LOG = LoggerFactory.getLogger(CustomProcessEngineExtension.class);

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    LOG.debug("set mocked deploymentId");
    deploymentId = "mockedDeploymentId";
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    LOG.debug("no undeployment needed");
  }

  public static CustomProcessEngineExtension builder() {
    return new CustomProcessEngineExtension();
  }

}
