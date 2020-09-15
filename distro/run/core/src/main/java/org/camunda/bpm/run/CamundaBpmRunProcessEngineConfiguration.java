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
package org.camunda.bpm.run;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.telemetry.CamundaIntegration;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

public class CamundaBpmRunProcessEngineConfiguration extends SpringProcessEngineConfiguration {


  @Inject
  private Environment env;

  public CamundaBpmRunProcessEngineConfiguration() {
    setDeployChangedOnly(true);
  }

  @Override
  protected String getFileResourceName(Resource resource) {
    // only path relative to the root deployment directory as identifier to
    // prevent re-deployments when the path changes (e.g. distro is moved)
    try {
      String deploymentDir = env.getProperty(CamundaBpmRunDeploymentConfiguration.CAMUNDA_DEPLOYMENT_DIR_PROPERTY);
      if(File.separator.equals("\\")) {
        deploymentDir = deploymentDir.replace("\\", "/");
      }
      String resourceAbsolutePath = resource.getURI().toString();
      int startIndex = resourceAbsolutePath.indexOf(deploymentDir) + deploymentDir.length();
      return resourceAbsolutePath.substring(startIndex);
    } catch (IOException e) {
      throw new ProcessEngineException("Failed to locate resource " + resource.getFilename(), e);
    }
  }

  @Override
  protected void initTelemetryData() {
    super.initTelemetryData();
    Set<String> camundaIntegration = telemetryData.getProduct().getInternals().getCamundaIntegration();
    camundaIntegration.add(CamundaIntegration.CAMUNDA_BPM_RUN);
  }
}
